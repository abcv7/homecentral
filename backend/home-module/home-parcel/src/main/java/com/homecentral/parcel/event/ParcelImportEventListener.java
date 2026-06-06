package com.homecentral.parcel.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecentral.auth.api.vo.MemberVO;
import com.homecentral.common.model.Result;
import com.homecentral.parcel.api.enums.ParcelStatus;
import com.homecentral.parcel.api.feign.AuthFeignClient;
import com.homecentral.parcel.api.vo.TrackingVO;
import com.homecentral.parcel.entity.Parcel;
import com.homecentral.parcel.mapper.ParcelMapper;
import com.homecentral.parcel.tracking.AliyunExpressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class ParcelImportEventListener {

    private static final Logger log = LoggerFactory.getLogger(ParcelImportEventListener.class);

    private final ParcelMapper parcelMapper;
    private final AliyunExpressService aliyunExpressService;
    private final AuthFeignClient authFeignClient;
    private final ObjectMapper objectMapper;

    public ParcelImportEventListener(ParcelMapper parcelMapper,
                                      AliyunExpressService aliyunExpressService,
                                      AuthFeignClient authFeignClient,
                                      ObjectMapper objectMapper) {
        this.parcelMapper = parcelMapper;
        this.aliyunExpressService = aliyunExpressService;
        this.authFeignClient = authFeignClient;
        this.objectMapper = objectMapper;
    }

    @EventListener
    @Async("parcelTaskExecutor")
    public void handleParcelImported(ParcelImportedEvent event) {
        log.info("Processing tracking refresh for {} imported parcels", event.getSuccessParcelIds().size());

        for (Long parcelId : event.getSuccessParcelIds()) {
            try {
                Parcel parcel = parcelMapper.selectById(parcelId);
                if (parcel == null || parcel.getTrackingNumber() == null) continue;

                String phoneTail = getPhoneTail(parcel.getCreatedBy());
                TrackingVO tracking = aliyunExpressService.queryWithDiscern(
                    parcel.getTrackingNumber(), phoneTail);

                parcelMapper.updateApiTrackingRaw(parcelId, objectMapper.writeValueAsString(tracking));

                if (tracking.getCourierName() != null && !tracking.getCourierName().isBlank()
                        && !tracking.getCourierName().equals(parcel.getCourierCompany())) {
                    log.info("Parcel {} courierCompany updated: {} -> {}", parcelId,
                            parcel.getCourierCompany(), tracking.getCourierName());
                    parcel.setCourierCompany(tracking.getCourierName());
                }

                if ("3".equals(tracking.getState()) || "SIGN".equals(tracking.getState())) {
                    parcel.setStatus(ParcelStatus.RECEIVED.name());
                    log.info("Parcel {} tracking shows delivered, updating status", parcelId);
                }

                parcel.setUpdatedAt(OffsetDateTime.now());
                parcelMapper.updateById(parcel);
                log.info("Parcel {} tracking refreshed", parcelId);
            } catch (Exception e) {
                log.warn("Failed to refresh tracking for parcel {}: {}", parcelId, e.getMessage());
            }
        }
    }

    private String getPhoneTail(Long userId) {
        if (userId == null) return null;
        try {
            Result<MemberVO> memberResult = authFeignClient.getMemberById(userId);
            if (memberResult != null && memberResult.getData() != null
                && memberResult.getData().getPhone() != null) {
                String phone = memberResult.getData().getPhone();
                if (phone.length() >= 4) {
                    return phone.substring(phone.length() - 4);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get phone tail for user {}: {}", userId, e.getMessage());
        }
        return null;
    }
}
