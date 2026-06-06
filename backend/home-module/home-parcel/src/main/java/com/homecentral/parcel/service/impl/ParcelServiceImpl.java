package com.homecentral.parcel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.auth.api.vo.MemberVO;
import com.homecentral.common.model.BatchResult;
import com.homecentral.common.model.Result;
import com.homecentral.friend.api.feign.FriendClient;
import com.homecentral.parcel.api.feign.AuthFeignClient;
import com.homecentral.parcel.api.dto.ParcelCreateRequest;
import com.homecentral.parcel.api.dto.ParcelUpdateRequest;
import com.homecentral.parcel.api.enums.ParcelStatus;
import com.homecentral.parcel.api.vo.ParcelSummaryVO;
import com.homecentral.parcel.api.vo.SharedParcelUserVO;
import com.homecentral.parcel.api.vo.TrackingVO;
import com.homecentral.parcel.entity.Parcel;
import com.homecentral.parcel.entity.ParcelShare;
import com.homecentral.parcel.event.ParcelImportedEvent;
import com.homecentral.parcel.mapper.ParcelMapper;
import com.homecentral.parcel.mapper.ParcelShareMapper;
import com.homecentral.parcel.service.IParcelService;
import com.homecentral.parcel.tracking.AliyunExpressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ParcelServiceImpl implements IParcelService {

    private static final Logger log = LoggerFactory.getLogger(ParcelServiceImpl.class);

    private final ParcelMapper parcelMapper;
    private final ParcelShareMapper parcelShareMapper;
    private final AliyunExpressService aliyunExpressService;
    private final AuthFeignClient authFeignClient;
    private final FriendClient friendClient;
    private final ApplicationEventPublisher eventPublisher;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> PLACEHOLDER_TOKENS = Set.of(
            "未知", "unknown", "undefined", "null");

    private static boolean isPlaceholder(String raw) {
        if (raw == null) return true;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return true;
        return PLACEHOLDER_TOKENS.contains(trimmed.toLowerCase());
    }

    private static TrackingVO buildPlaceholderVO(String rawTrackingNumber) {
        TrackingVO vo = new TrackingVO();
        vo.setTrackingNumber(rawTrackingNumber);
        vo.setState("no_tracking_number");
        vo.setStateLabel("暂无具体单号");
        vo.setMessage("该包裹尚未提供具体运单号");
        vo.setTracks(List.of());
        return vo;
    }

    public ParcelServiceImpl(ParcelMapper parcelMapper, ParcelShareMapper parcelShareMapper,
                             AliyunExpressService aliyunExpressService,
                             AuthFeignClient authFeignClient,
                             FriendClient friendClient,
                             ApplicationEventPublisher eventPublisher) {
        this.parcelMapper = parcelMapper;
        this.parcelShareMapper = parcelShareMapper;
        this.aliyunExpressService = aliyunExpressService;
        this.authFeignClient = authFeignClient;
        this.friendClient = friendClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ParcelSummaryVO create(ParcelCreateRequest request, Long userId) {
        Parcel parcel = new Parcel();
        parcel.setCourierCompany(request.getCourierCompany());
        parcel.setTrackingNumber(request.getTrackingNumber());
        parcel.setPickupCode(request.getPickupCode());
        parcel.setRemark(request.getRemark());
        parcel.setAttachmentUrl(request.getAttachmentUrl());
        parcel.setStatus(ParcelStatus.PENDING_PICKUP.name());
        parcel.setCreatedBy(userId);
        parcel.setCreatedAt(OffsetDateTime.now());
        parcel.setUpdatedAt(OffsetDateTime.now());

        parcel.setSource("MANUAL");
        parcel.setOwnerName(request.getOwnerName());
        parcel.setOwnerUserId(userId);
        LocalDate arrived = request.getArrivedDate();
        parcel.setArrivedDate(arrived);
        parcel.setDaysAtStation(arrived != null ? (int) ChronoUnit.DAYS.between(arrived, LocalDate.now()) : 0);

        parcelMapper.insert(parcel);
        return toVO(parcel);
    }

    @Override
    @Transactional
    public BatchResult<ParcelSummaryVO> batchImport(List<ParcelCreateRequest> requests, Long userId) {
        List<ParcelSummaryVO> successItems = new ArrayList<>();
        List<BatchResult.FailureItem> failureItems = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            ParcelCreateRequest req = requests.get(i);
            try {
                validateTrackingNumberUnique(req.getTrackingNumber(), null);
                validatePickupCodeUnique(req.getPickupCode(), null);

                Parcel parcel = new Parcel();
                parcel.setCourierCompany(req.getCourierCompany());
                parcel.setTrackingNumber(req.getTrackingNumber());
                parcel.setPickupCode(req.getPickupCode());
                parcel.setProductName(req.getProductName());
                parcel.setRemark(req.getRemark());
                parcel.setAttachmentUrl(req.getAttachmentUrl());
                parcel.setStatus(ParcelStatus.PENDING_PICKUP.name());
                parcel.setSource("OCR");
                parcel.setCreatedBy(userId);
                parcel.setOwnerUserId(userId);
                parcel.setOwnerName(req.getOwnerName());
                parcel.setCreatedAt(OffsetDateTime.now());
                parcel.setUpdatedAt(OffsetDateTime.now());
                LocalDate arrived = req.getArrivedDate();
                parcel.setArrivedDate(arrived);
                parcel.setDaysAtStation(arrived != null ?
                    (int) ChronoUnit.DAYS.between(arrived, LocalDate.now()) : 0);

                parcelMapper.insert(parcel);
                successItems.add(toVO(parcel));
            } catch (Exception e) {
                failureItems.add(new BatchResult.FailureItem(
                    i,
                    "取件码: " + req.getPickupCode() + ", 快递公司: " + req.getCourierCompany(),
                    e.getMessage()
                ));
            }
        }

        if (!successItems.isEmpty()) {
            List<Long> successIds = successItems.stream()
                .map(ParcelSummaryVO::getId).toList();
            eventPublisher.publishEvent(new ParcelImportedEvent(this, successIds));
        }

        return BatchResult.of(successItems, failureItems);
    }

    private void validatePickupCodeUnique(String pickupCode, Long excludeId) {
        if (pickupCode == null || pickupCode.isBlank()) return;
        LambdaQueryWrapper<Parcel> wrapper = new LambdaQueryWrapper<Parcel>()
            .eq(Parcel::getPickupCode, pickupCode)
            .eq(Parcel::getDeleted, false);
        if (excludeId != null) {
            wrapper.ne(Parcel::getId, excludeId);
        }
        if (parcelMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("取件码 " + pickupCode + " 已被占用");
        }
    }

    private void validateTrackingNumberUnique(String trackingNumber, Long excludeId) {
        if (trackingNumber == null || trackingNumber.isBlank()) return;
        LambdaQueryWrapper<Parcel> wrapper = new LambdaQueryWrapper<Parcel>()
            .eq(Parcel::getTrackingNumber, trackingNumber)
            .eq(Parcel::getDeleted, false);
        if (excludeId != null) {
            wrapper.ne(Parcel::getId, excludeId);
        }
        if (parcelMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("运单号 " + trackingNumber + " 已被占用");
        }
    }

    @Override
    public ParcelSummaryVO getById(Long parcelId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new RuntimeException("快递不存在");
        }
        return toVO(parcel);
    }

    @Override
    public Page<ParcelSummaryVO> list(Long userId, String status, String trackingNumber, int page, int size) {
        // admin (userId=1) sees all parcels
        boolean isAdmin = userId != null && userId == 1L;
        Set<Long> visibleUserIds = isAdmin ? null : getVisibleUserIds(userId);
        List<Long> sharedParcelIds = isAdmin ? null : getSharedParcelIds(userId);

        LambdaQueryWrapper<Parcel> wrapper = new LambdaQueryWrapper<>();
        if (!isAdmin) {
            wrapper.and(w -> {
                w.in(Parcel::getCreatedBy, visibleUserIds)
                 .or().in(Parcel::getOwnerUserId, visibleUserIds);
                if (!sharedParcelIds.isEmpty()) {
                    w.or().in(Parcel::getId, sharedParcelIds);
                }
            });
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Parcel::getStatus, status);
        }
        if (StringUtils.hasText(trackingNumber)) {
            wrapper.like(Parcel::getTrackingNumber, trackingNumber);
        }
        wrapper.orderByDesc(Parcel::getDaysAtStation);
        wrapper.orderByDesc(Parcel::getCreatedAt);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Parcel> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        mpPage = parcelMapper.selectPage(mpPage, wrapper);

        Map<Long, Integer> shareCountMap = batchShareCounts(mpPage.getRecords().stream().map(Parcel::getId).toList());

        List<ParcelSummaryVO> records = mpPage.getRecords().stream()
                .map(p -> toVOWithShareCount(p, shareCountMap.getOrDefault(p.getId(), 0)))
                .toList();
        return new PageImpl<>(records, PageRequest.of((int) page - 1, size), mpPage.getTotal());
    }

    private Map<Long, Integer> batchShareCounts(List<Long> parcelIds) {
        if (parcelIds.isEmpty()) return Map.of();
        List<ParcelShare> shares = parcelShareMapper.selectList(
                new LambdaQueryWrapper<ParcelShare>().in(ParcelShare::getParcelId, parcelIds));
        Map<Long, Integer> result = new HashMap<>();
        for (ParcelShare s : shares) {
            result.merge(s.getParcelId(), 1, Integer::sum);
        }
        return result;
    }

    @Override
    @Transactional
    public ParcelSummaryVO update(Long parcelId, ParcelUpdateRequest request, Long userId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new RuntimeException("快递不存在");
        }
        if (!ParcelStatus.PENDING_PICKUP.name().equals(parcel.getStatus())) {
            throw new RuntimeException("已取件的快递不能修改");
        }
        if (request.getCourierCompany() != null) {
            parcel.setCourierCompany(request.getCourierCompany());
        }
        if (request.getTrackingNumber() != null) {
            parcel.setTrackingNumber(request.getTrackingNumber());
        }
        if (request.getPickupCode() != null) {
            parcel.setPickupCode(request.getPickupCode());
        }
        if (request.getRemark() != null) {
            parcel.setRemark(request.getRemark());
        }
        if (request.getAttachmentUrl() != null) {
            parcel.setAttachmentUrl(request.getAttachmentUrl());
        }
        if (request.getOwnerName() != null) {
            parcel.setOwnerName(request.getOwnerName());
        }
        if (request.getArrivedDate() != null) {
            parcel.setArrivedDate(request.getArrivedDate());
            parcel.setDaysAtStation((int) ChronoUnit.DAYS.between(request.getArrivedDate(), LocalDate.now()));
        }
        parcel.setUpdatedAt(OffsetDateTime.now());
        parcelMapper.updateById(parcel);
        return toVO(parcel);
    }

    @Override
    @Transactional
    public ParcelSummaryVO pickUp(Long parcelId, Long userId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new RuntimeException("快递不存在");
        }
        if (!ParcelStatus.PENDING_PICKUP.name().equals(parcel.getStatus())) {
            throw new RuntimeException("快递已被取件");
        }
        parcel.setStatus(ParcelStatus.PICKED_UP.name());
        parcel.setUpdatedAt(OffsetDateTime.now());
        parcelMapper.updateById(parcel);
        return toVO(parcel);
    }

    @Override
    @Transactional
    public ParcelSummaryVO receive(Long parcelId, Long userId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new RuntimeException("快递不存在");
        }
        if (!ParcelStatus.PICKED_UP.name().equals(parcel.getStatus())) {
            throw new RuntimeException("快递状态不是已取件");
        }
        parcel.setStatus(ParcelStatus.RECEIVED.name());
        parcel.setUpdatedAt(OffsetDateTime.now());
        parcelMapper.updateById(parcel);
        return toVO(parcel);
    }

    @Override
    @Transactional
    public void delete(Long parcelId, Long userId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new RuntimeException("快递不存在");
        }
        if (!ParcelStatus.RECEIVED.name().equals(parcel.getStatus())) {
            throw new RuntimeException("仅已收货的快递可以删除");
        }
        parcelMapper.deleteById(parcelId);
    }

    @Override
    @Transactional
    public void shareParcel(Long parcelId, Long targetUserId, Long userId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new RuntimeException("快递不存在");
        }
        boolean isOwner = userId.equals(parcel.getCreatedBy()) || userId.equals(parcel.getOwnerUserId());
        if (!isOwner) {
            throw new RuntimeException("无权分享此快递");
        }
        ParcelShare existing = parcelShareMapper.selectOne(
                new LambdaQueryWrapper<ParcelShare>()
                        .eq(ParcelShare::getParcelId, parcelId)
                        .eq(ParcelShare::getSharedWithUserId, targetUserId));
        if (existing != null) {
            return;
        }
        ParcelShare share = new ParcelShare();
        share.setParcelId(parcelId);
        share.setSharedWithUserId(targetUserId);
        share.setCreatedAt(OffsetDateTime.now());
        parcelShareMapper.insert(share);
    }

    @Override
    @Transactional
    public void unshareParcel(Long parcelId, Long targetUserId, Long userId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new RuntimeException("快递不存在");
        }
        boolean isOwner = userId.equals(parcel.getCreatedBy()) || userId.equals(parcel.getOwnerUserId());
        if (!isOwner) {
            throw new RuntimeException("无权取消分享");
        }
        parcelShareMapper.delete(
                new LambdaQueryWrapper<ParcelShare>()
                        .eq(ParcelShare::getParcelId, parcelId)
                        .eq(ParcelShare::getSharedWithUserId, targetUserId));
    }

    @Override
    public TrackingVO queryTracking(Long parcelId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new RuntimeException("快递不存在");
        }
        String trackingNumber = parcel.getTrackingNumber();
        if (isPlaceholder(trackingNumber)) {
            log.info("Parcel {} tracking number is placeholder, skip Aliyun query (value='{}')",
                    parcelId, trackingNumber);
            return buildPlaceholderVO(trackingNumber);
        }
        String phoneTail = null;
        if (parcel.getCreatedBy() != null) {
            try {
                Result<MemberVO> memberResult = authFeignClient.getMemberById(parcel.getCreatedBy());
                if (memberResult != null && memberResult.getData() != null && memberResult.getData().getPhone() != null) {
                    String phone = memberResult.getData().getPhone();
                    if (phone.length() >= 4) {
                        phoneTail = phone.substring(phone.length() - 4);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to get member phone for parcel {}: {}", parcelId, e.getMessage());
            }
        }

        TrackingVO tracking = aliyunExpressService.queryWithDiscern(trackingNumber, phoneTail, parcel.getCreatedBy());

        syncParcelFromTracking(parcel, tracking);

        return tracking;
    }

    private void syncParcelFromTracking(Parcel parcel, TrackingVO tracking) {
        boolean changed = false;

        if (tracking.getCourierName() != null && !tracking.getCourierName().isBlank()
                && !tracking.getCourierName().equals(parcel.getCourierCompany())) {
            log.info("Parcel {} courierCompany updated: {} -> {}", parcel.getId(),
                    parcel.getCourierCompany(), tracking.getCourierName());
            parcel.setCourierCompany(tracking.getCourierName());
            changed = true;
        }

        if (isReceivedState(tracking.getState())
                && ParcelStatus.PENDING_PICKUP.name().equals(parcel.getStatus())) {
            log.info("Parcel {} status updated: {} -> {} (state={})", parcel.getId(),
                    parcel.getStatus(), ParcelStatus.RECEIVED.name(), tracking.getState());
            parcel.setStatus(ParcelStatus.RECEIVED.name());
            changed = true;
        }

        if (changed) {
            parcel.setUpdatedAt(OffsetDateTime.now());
            parcelMapper.updateById(parcel);
        }

        try {
            parcelMapper.updateApiTrackingRaw(parcel.getId(),
                    objectMapper.writeValueAsString(tracking));
        } catch (Exception e) {
            log.warn("Failed to save tracking raw for parcel {}: {}", parcel.getId(), e.getMessage());
        }
    }

    private static boolean isReceivedState(String state) {
        return "3".equals(state) || "SIGN".equals(state);
    }

    @Override
    @Transactional
    public void refreshDaysAtStation() {
        List<Parcel> parcels = parcelMapper.selectList(
                new LambdaQueryWrapper<Parcel>().isNotNull(Parcel::getArrivedDate));
        for (Parcel parcel : parcels) {
            int days = (int) ChronoUnit.DAYS.between(parcel.getArrivedDate(), LocalDate.now());
            if (days != parcel.getDaysAtStation()) {
                parcel.setDaysAtStation(days);
                parcelMapper.updateById(parcel);
            }
        }
    }

    private Set<Long> getVisibleUserIds(Long userId) {
        Set<Long> ids = new java.util.HashSet<>();
        ids.add(userId);
        try {
            Result<List<Long>> visibleResult = friendClient.getVisibleUsers(userId);
            if (visibleResult != null && visibleResult.isSuccess() && visibleResult.getData() != null) {
                ids.addAll(visibleResult.getData());
            }
        } catch (Exception e) {
            log.warn("[parcel] getVisibleUsers failed for user={} ({}). Falling back to self-only.",
                    userId, e.getMessage());
        }
        return ids;
    }

    private List<Long> getSharedParcelIds(Long userId) {
        return parcelShareMapper.selectList(
                new LambdaQueryWrapper<ParcelShare>()
                        .eq(ParcelShare::getSharedWithUserId, userId))
                .stream().map(ParcelShare::getParcelId).toList();
    }

    private ParcelSummaryVO toVO(Parcel parcel) {
        return toVOWithShareCount(parcel, countShares(parcel.getId()));
    }

    private int countShares(Long parcelId) {
        Long n = parcelShareMapper.selectCount(
                new LambdaQueryWrapper<ParcelShare>().eq(ParcelShare::getParcelId, parcelId));
        return n == null ? 0 : n.intValue();
    }

    private ParcelSummaryVO toVOWithShareCount(Parcel parcel, int sharedCount) {
        ParcelSummaryVO vo = new ParcelSummaryVO();
        vo.setId(parcel.getId());
        vo.setCourierCompany(parcel.getCourierCompany());
        vo.setTrackingNumber(parcel.getTrackingNumber());
        vo.setPickupCode(parcel.getPickupCode());
        vo.setRemark(parcel.getRemark());
        vo.setAttachmentUrl(parcel.getAttachmentUrl());
        vo.setStatus(ParcelStatus.valueOf(parcel.getStatus()));
        vo.setCreatedBy(parcel.getCreatedBy());
        vo.setCreatedAt(parcel.getCreatedAt());
        vo.setSource(parcel.getSource());
        vo.setOwnerName(parcel.getOwnerName());
        vo.setDaysAtStation(parcel.getDaysAtStation());
        vo.setProductName(parcel.getProductName());
        vo.setSharedCount(sharedCount);
        return vo;
    }

    @Override
    public List<SharedParcelUserVO> listSharedUsers(Long parcelId, Long userId) {
        Parcel parcel = parcelMapper.selectById(parcelId);
        if (parcel == null) {
            throw new RuntimeException("快递不存在");
        }
        boolean isOwner = userId == null
                || userId.equals(parcel.getCreatedBy())
                || userId.equals(parcel.getOwnerUserId());
        if (!isOwner) {
            throw new RuntimeException("无权查看此快递的分享列表");
        }
        List<ParcelShare> shares = parcelShareMapper.selectList(
                new LambdaQueryWrapper<ParcelShare>()
                        .eq(ParcelShare::getParcelId, parcelId)
                        .orderByDesc(ParcelShare::getCreatedAt));
        if (shares.isEmpty()) return List.of();
        return shares.stream().map(share -> {
            SharedParcelUserVO vo = new SharedParcelUserVO();
            vo.setUserId(share.getSharedWithUserId());
            vo.setSharedAt(share.getCreatedAt());
            try {
                Result<MemberVO> r = authFeignClient.getMemberById(share.getSharedWithUserId());
                if (r != null && r.isSuccess() && r.getData() != null) {
                    vo.setNickname(r.getData().getNickname());
                    vo.setEmail(r.getData().getEmail());
                    vo.setPhone(r.getData().getPhone());
                }
            } catch (Exception e) {
                log.warn("[parcel] getMemberById failed for share target user={}: {}",
                        share.getSharedWithUserId(), e.getMessage());
            }
            return vo;
        }).collect(Collectors.toList());
    }
}
