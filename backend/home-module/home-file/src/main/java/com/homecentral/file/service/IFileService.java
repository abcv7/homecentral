package com.homecentral.file.service;

import com.homecentral.file.api.dto.FileUploadRequest;
import com.homecentral.file.api.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {

    FileUploadVO upload(MultipartFile file, String businessType, String businessId, Long userId);

    FileUploadVO getByObjectKey(String objectKey);
}
