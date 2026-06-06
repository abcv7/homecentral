package com.homecentral.parcel.service;

import com.homecentral.parcel.api.dto.ParcelCreateRequest;
import com.homecentral.parcel.api.vo.ParcelRecognitionResult;
import com.homecentral.parcel.api.vo.PendingRecognitionVO;

import java.util.List;

public interface IPendingRecognitionService {

    Long createProcessing(Long userId, String inputHash);

    void markCompleted(Long id, ParcelRecognitionResult result);

    void markFailed(Long id, String message);

    void markImported(Long id, String failureMessage);

    void markCancelled(Long id);

    List<PendingRecognitionVO> listPending(Long userId);

    PendingRecognitionVO getById(Long id, Long userId);

    ParcelRecognitionResult parseResult(String resultJson);
}
