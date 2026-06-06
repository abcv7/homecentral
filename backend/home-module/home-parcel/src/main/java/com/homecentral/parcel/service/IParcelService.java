package com.homecentral.parcel.service;

import com.homecentral.common.model.BatchResult;
import com.homecentral.parcel.api.dto.ParcelCreateRequest;
import com.homecentral.parcel.api.dto.ParcelUpdateRequest;
import com.homecentral.parcel.api.vo.ParcelSummaryVO;
import com.homecentral.parcel.api.vo.SharedParcelUserVO;
import com.homecentral.parcel.api.vo.TrackingVO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IParcelService {

    ParcelSummaryVO create(ParcelCreateRequest request, Long userId);

    BatchResult<ParcelSummaryVO> batchImport(List<ParcelCreateRequest> requests, Long userId);

    ParcelSummaryVO getById(Long parcelId);

    Page<ParcelSummaryVO> list(Long userId, String status, String trackingNumber, int page, int size);

    ParcelSummaryVO update(Long parcelId, ParcelUpdateRequest request, Long userId);

    ParcelSummaryVO pickUp(Long parcelId, Long userId);

    ParcelSummaryVO receive(Long parcelId, Long userId);

    void delete(Long parcelId, Long userId);

    void shareParcel(Long parcelId, Long targetUserId, Long userId);

    void unshareParcel(Long parcelId, Long targetUserId, Long userId);

    List<SharedParcelUserVO> listSharedUsers(Long parcelId, Long userId);

    void refreshDaysAtStation();

    TrackingVO queryTracking(Long parcelId);
}
