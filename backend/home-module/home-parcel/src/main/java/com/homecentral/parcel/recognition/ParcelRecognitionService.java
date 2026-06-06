package com.homecentral.parcel.recognition;

import com.homecentral.parcel.api.vo.ParcelRecognitionResult;
import com.homecentral.parcel.api.vo.ParcelRecognitionVO;
import com.homecentral.parcel.service.IPendingRecognitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
public class ParcelRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(ParcelRecognitionService.class);

    private final VlmClient vlmClient;
    private final OcrClient ocrClient;
    private final RecognitionMerger merger;
    private final IPendingRecognitionService pendingService;

    public ParcelRecognitionService(VlmClient vlmClient, OcrClient ocrClient,
                                     RecognitionMerger merger,
                                     IPendingRecognitionService pendingService) {
        this.vlmClient = vlmClient;
        this.ocrClient = ocrClient;
        this.merger = merger;
        this.pendingService = pendingService;
    }

    public ParcelRecognitionResult recognize(Long userId, String imageBase64) {
        if (userId == null) {
            return recognizeInternal(imageBase64);
        }

        Long pendingId = pendingService.createProcessing(userId, hash(imageBase64));
        try {
            ParcelRecognitionResult result = recognizeInternal(imageBase64);
            result.setPendingId(pendingId);
            pendingService.markCompleted(pendingId, result);
            return result;
        } catch (Exception e) {
            log.error("Image recognition failed for pending id={}", pendingId, e);
            pendingService.markFailed(pendingId, e.getMessage());
            throw e;
        }
    }

    public ParcelRecognitionResult recognizeText(Long userId, String smsText) {
        if (userId == null) {
            return recognizeTextInternal(smsText);
        }

        Long pendingId = pendingService.createProcessing(userId, hash(smsText));
        try {
            ParcelRecognitionResult result = recognizeTextInternal(smsText);
            result.setPendingId(pendingId);
            pendingService.markCompleted(pendingId, result);
            return result;
        } catch (Exception e) {
            log.error("Text recognition failed for pending id={}", pendingId, e);
            pendingService.markFailed(pendingId, e.getMessage());
            throw e;
        }
    }

    private ParcelRecognitionResult recognizeInternal(String imageBase64) {
        log.info("Starting image recognition, base64 length={}", imageBase64.length());
        List<ParcelRecognitionVO> vlmResults = callWithRetry(() -> vlmClient.recognize(imageBase64), "VLM.image");
        String ocrText = ocrClient.recognize(imageBase64);
        List<ParcelRecognitionVO> merged = merger.merge(vlmResults, ocrText);
        ParcelRecognitionResult result = new ParcelRecognitionResult(merged);
        result.setRawOcrText(ocrText);
        log.info("Image recognition complete, found {} parcels", merged.size());
        return result;
    }

    private ParcelRecognitionResult recognizeTextInternal(String smsText) {
        log.info("Starting text recognition, text length={}", smsText.length());
        List<ParcelRecognitionVO> vlmResults = callWithRetry(() -> vlmClient.recognizeText(smsText), "VLM.text");
        List<ParcelRecognitionVO> merged = merger.merge(vlmResults, null);
        ParcelRecognitionResult result = new ParcelRecognitionResult(merged);
        result.setRawOcrText(smsText);
        log.info("Text recognition complete, found {} parcels", merged.size());
        return result;
    }

    private <T> T callWithRetry(Supplier<T> call, String label) {
        try {
            return call.get();
        } catch (Exception first) {
            log.warn("{} failed, retrying once. Error: {}", label, first.getMessage());
            try {
                return call.get();
            } catch (Exception second) {
                log.error("{} failed after retry. First: {} | Second: {}", label, first.getMessage(), second.getMessage());
                throw new RuntimeException(label + " 调用失败: " + second.getMessage(), second);
            }
        }
    }

    private String hash(String input) {
        if (input == null) return null;
        int h = input.hashCode();
        return Integer.toHexString(h) + "_" + input.length();
    }
}
