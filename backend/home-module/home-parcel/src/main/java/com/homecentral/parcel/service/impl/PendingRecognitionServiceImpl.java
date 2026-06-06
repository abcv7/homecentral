package com.homecentral.parcel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecentral.common.model.BatchResult;
import com.homecentral.parcel.api.dto.ParcelCreateRequest;
import com.homecentral.parcel.api.vo.ParcelRecognitionResult;
import com.homecentral.parcel.api.vo.ParcelRecognitionVO;
import com.homecentral.parcel.api.vo.ParcelSummaryVO;
import com.homecentral.parcel.api.vo.PendingRecognitionVO;
import com.homecentral.parcel.entity.PendingRecognition;
import com.homecentral.parcel.mapper.PendingRecognitionMapper;
import com.homecentral.parcel.service.IParcelService;
import com.homecentral.parcel.service.IPendingRecognitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PendingRecognitionServiceImpl implements IPendingRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(PendingRecognitionServiceImpl.class);

    private static final int TTL_HOURS = 24;
    private static final int CANCELLED_TTL_HOURS = 24;
    private static final int IMPORTED_TTL_DAYS = 7;
    private static final int PROCESSING_TIMEOUT_MINUTES = 30;

    private final PendingRecognitionMapper pendingMapper;
    private final IParcelService parcelService;
    private final ObjectMapper objectMapper;

    public PendingRecognitionServiceImpl(PendingRecognitionMapper pendingMapper,
                                          IParcelService parcelService,
                                          ObjectMapper objectMapper) {
        this.pendingMapper = pendingMapper;
        this.parcelService = parcelService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Long createProcessing(Long userId, String inputHash) {
        PendingRecognition p = new PendingRecognition();
        p.setUserId(userId);
        p.setStatus("processing");
        p.setInputHash(inputHash);
        p.setCreatedAt(OffsetDateTime.now());
        pendingMapper.insert(p);
        return p.getId();
    }

    @Override
    public void markCompleted(Long id, ParcelRecognitionResult result) {
        String json;
        try {
            json = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize recognition result for pending id={}", id, e);
            markFailed(id, "结果序列化失败");
            return;
        }
        PendingRecognition update = new PendingRecognition();
        update.setId(id);
        update.setStatus("completed");
        update.setResultJson(json);
        update.setCompletedAt(OffsetDateTime.now());
        update.setExpiresAt(OffsetDateTime.now().plusHours(TTL_HOURS));
        pendingMapper.updateById(update);
    }

    @Override
    public void markFailed(Long id, String message) {
        PendingRecognition update = new PendingRecognition();
        update.setId(id);
        update.setStatus("failed");
        update.setFailureMessage(truncate(message, 1000));
        update.setCompletedAt(OffsetDateTime.now());
        pendingMapper.updateById(update);
    }

    @Override
    public void markImported(Long id, String failureMessage) {
        PendingRecognition update = new PendingRecognition();
        update.setId(id);
        update.setStatus("imported");
        if (failureMessage != null) {
            update.setFailureMessage(truncate(failureMessage, 1000));
        }
        pendingMapper.updateById(update);
    }

    @Override
    public void markCancelled(Long id) {
        PendingRecognition update = new PendingRecognition();
        update.setId(id);
        update.setStatus("cancelled");
        pendingMapper.updateById(update);
    }

    @Override
    public List<PendingRecognitionVO> listPending(Long userId) {
        LambdaQueryWrapper<PendingRecognition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PendingRecognition::getUserId, userId)
               .in(PendingRecognition::getStatus, "processing", "completed", "failed")
               .orderByDesc(PendingRecognition::getCreatedAt);
        return pendingMapper.selectList(wrapper).stream()
            .map(this::toVO)
            .collect(Collectors.toList());
    }

    @Override
    public PendingRecognitionVO getById(Long id, Long userId) {
        PendingRecognition p = pendingMapper.selectById(id);
        if (p == null || !p.getUserId().equals(userId)) return null;
        return toVO(p);
    }

    @Override
    public ParcelRecognitionResult parseResult(String resultJson) {
        if (resultJson == null || resultJson.isBlank()) return null;
        try {
            return objectMapper.readValue(resultJson, ParcelRecognitionResult.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse resultJson", e);
            return null;
        }
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000L, initialDelay = 60 * 1000L)
    public void scheduledScan() {
        try {
            int timeoutCount = markProcessingTimeout();
            int deletedCancelled = deleteOldCancelled();
            int deletedImported = deleteOldImported();
            int autoImported = autoImportExpired();
            if (timeoutCount + deletedCancelled + deletedImported + autoImported > 0) {
                log.info("PendingRecognition scheduled scan: timeout={}, delCancelled={}, delImported={}, autoImported={}",
                    timeoutCount, deletedCancelled, deletedImported, autoImported);
            }
        } catch (Exception e) {
            log.error("Scheduled scan failed", e);
        }
    }

    int markProcessingTimeout() {
        OffsetDateTime threshold = OffsetDateTime.now().minusMinutes(PROCESSING_TIMEOUT_MINUTES);
        LambdaQueryWrapper<PendingRecognition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PendingRecognition::getStatus, "processing")
               .lt(PendingRecognition::getCreatedAt, threshold);
        List<PendingRecognition> stale = pendingMapper.selectList(wrapper);
        for (PendingRecognition p : stale) {
            PendingRecognition update = new PendingRecognition();
            update.setId(p.getId());
            update.setStatus("failed");
            update.setFailureMessage("AI 识别超时（超过 " + PROCESSING_TIMEOUT_MINUTES + " 分钟）");
            update.setCompletedAt(OffsetDateTime.now());
            pendingMapper.updateById(update);
        }
        return stale.size();
    }

    int deleteOldCancelled() {
        OffsetDateTime threshold = OffsetDateTime.now().minusHours(CANCELLED_TTL_HOURS);
        LambdaQueryWrapper<PendingRecognition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PendingRecognition::getStatus, "cancelled")
               .lt(PendingRecognition::getCreatedAt, threshold);
        return pendingMapper.delete(wrapper);
    }

    int deleteOldImported() {
        OffsetDateTime threshold = OffsetDateTime.now().minusDays(IMPORTED_TTL_DAYS);
        LambdaQueryWrapper<PendingRecognition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PendingRecognition::getStatus, "imported")
               .lt(PendingRecognition::getCreatedAt, threshold);
        return pendingMapper.delete(wrapper);
    }

    int autoImportExpired() {
        OffsetDateTime now = OffsetDateTime.now();
        LambdaQueryWrapper<PendingRecognition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PendingRecognition::getStatus, "completed")
               .isNotNull(PendingRecognition::getExpiresAt)
               .lt(PendingRecognition::getExpiresAt, now);
        List<PendingRecognition> expired = pendingMapper.selectList(wrapper);

        int count = 0;
        for (PendingRecognition p : expired) {
            try {
                if (doAutoImport(p)) {
                    count++;
                }
            } catch (Exception e) {
                log.error("Auto import failed for pending id={}", p.getId(), e);
            }
        }
        return count;
    }

    private boolean doAutoImport(PendingRecognition p) {
        ParcelRecognitionResult result = parseResult(p.getResultJson());
        if (result == null || result.getParcels() == null || result.getParcels().isEmpty()) {
            markImported(p.getId(), "识别结果为空，跳过自动入库");
            return true;
        }

        List<ParcelCreateRequest> requests = new ArrayList<>();
        for (ParcelRecognitionVO vo : result.getParcels()) {
            ParcelCreateRequest req = new ParcelCreateRequest();
            req.setCourierCompany(vo.getCourierCompany());
            req.setTrackingNumber(vo.getTrackingNumber());
            req.setPickupCode(vo.getPickupCode());
            req.setOwnerName(vo.getOwnerName());
            req.setArrivedDate(vo.getArrivedDate());
            req.setProductName(vo.getProductName());
            requests.add(req);
        }

        BatchResult<ParcelSummaryVO> br = parcelService.batchImport(requests, p.getUserId());

        String failureMsg = null;
        if (br != null && br.getFailureItems() != null && !br.getFailureItems().isEmpty()) {
            failureMsg = br.getFailureItems().stream()
                .map(f -> f.getItem() + ": " + f.getReason())
                .collect(Collectors.joining("; "));
        }
        markImported(p.getId(), failureMsg);
        return true;
    }

    private PendingRecognitionVO toVO(PendingRecognition p) {
        PendingRecognitionVO vo = new PendingRecognitionVO();
        vo.setId(p.getId());
        vo.setStatus(p.getStatus());
        vo.setResultJson(p.getResultJson());
        vo.setFailureMessage(p.getFailureMessage());
        vo.setCreatedAt(p.getCreatedAt());
        vo.setCompletedAt(p.getCompletedAt());
        vo.setExpiresAt(p.getExpiresAt());
        return vo;
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}
