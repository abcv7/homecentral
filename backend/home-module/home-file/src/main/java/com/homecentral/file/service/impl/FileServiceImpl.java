package com.homecentral.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.file.api.vo.FileUploadVO;
import com.homecentral.file.entity.FileMetadata;
import com.homecentral.file.mapper.FileMetadataMapper;
import com.homecentral.file.service.IFileService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class FileServiceImpl implements IFileService {

    private final MinioClient minioClient;
    private final FileMetadataMapper fileMetadataMapper;

    public FileServiceImpl(MinioClient minioClient, FileMetadataMapper fileMetadataMapper) {
        this.minioClient = minioClient;
        this.fileMetadataMapper = fileMetadataMapper;
    }

    @Value("${app.minio.bucket}")
    private String bucket;

    @Override
    @Transactional
    public FileUploadVO upload(MultipartFile file, String businessType, String businessId, Long userId) {
        try {
            String objectKey = buildObjectKey(businessType, businessId, file.getOriginalFilename());

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            FileMetadata metadata = new FileMetadata();
            metadata.setObjectKey(objectKey);
            metadata.setOriginalFilename(file.getOriginalFilename());
            metadata.setContentType(file.getContentType());
            metadata.setFileSize(file.getSize());
            metadata.setBusinessType(businessType);
            metadata.setBusinessId(businessId);
            metadata.setBucket(bucket);
            metadata.setUploadedBy(userId);
            metadata.setCreatedAt(OffsetDateTime.now());
            fileMetadataMapper.insert(metadata);

            FileUploadVO vo = new FileUploadVO();
            vo.setFileId(metadata.getId().toString());
            vo.setObjectKey(objectKey);
            vo.setAccessUrl(getPresignedUrl(objectKey));
            return vo;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public FileUploadVO getByObjectKey(String objectKey) {
        LambdaQueryWrapper<FileMetadata> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileMetadata::getObjectKey, objectKey);
        FileMetadata metadata = fileMetadataMapper.selectOne(wrapper);
        if (metadata == null) {
            throw new RuntimeException("文件不存在");
        }
        FileUploadVO vo = new FileUploadVO();
        vo.setFileId(metadata.getId().toString());
        vo.setObjectKey(objectKey);
        vo.setAccessUrl(getPresignedUrl(objectKey));
        return vo;
    }

    private String buildObjectKey(String businessType, String businessId, String filename) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String ext = "";
        if (filename != null && filename.contains(".")) {
            ext = filename.substring(filename.lastIndexOf("."));
        }
        return String.format("%s/%s/%s%s",
                businessType != null ? businessType : "default",
                businessId != null ? businessId : "unknown",
                uuid, ext);
    }

    private String getPresignedUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(3600)
                            .build());
        } catch (Exception e) {
            return "/" + bucket + "/" + objectKey;
        }
    }
}
