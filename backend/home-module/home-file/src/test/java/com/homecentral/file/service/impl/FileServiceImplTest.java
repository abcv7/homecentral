package com.homecentral.file.service.impl;

import com.homecentral.file.api.vo.FileUploadVO;
import com.homecentral.file.entity.FileMetadata;
import com.homecentral.file.mapper.FileMetadataMapper;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private FileMetadataMapper fileMetadataMapper;

    @InjectMocks
    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "bucket", "homecentral-files");
    }

    @Test
    void shouldUploadFileSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test-image-content".getBytes());

        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);
        when(fileMetadataMapper.insert(any(FileMetadata.class))).thenAnswer(invocation -> {
            FileMetadata fm = invocation.getArgument(0);
            fm.setId(1L);
            return 1;
        });
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://localhost:9000/homecentral-files/test-key");

        FileUploadVO vo = fileService.upload(file, "avatar", "user_1", 1L);

        assertNotNull(vo);
        assertNotNull(vo.getFileId());
        assertEquals("1", vo.getFileId());
        assertTrue(vo.getObjectKey().contains("/user_1/"));
        assertTrue(vo.getAccessUrl().startsWith("http://"));

        verify(fileMetadataMapper).insert(any(FileMetadata.class));
    }

    @Test
    void shouldGetByObjectKeySuccess() throws Exception {
        FileMetadata metadata = new FileMetadata();
        metadata.setId(1L);
        metadata.setObjectKey("avatars/uuid.jpg");
        metadata.setOriginalFilename("test.jpg");
        metadata.setBucket("homecentral-files");

        when(fileMetadataMapper.selectOne(any())).thenReturn(metadata);
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://localhost:9000/homecentral-files/avatars/uuid.jpg");

        FileUploadVO vo = fileService.getByObjectKey("avatars/uuid.jpg");

        assertNotNull(vo);
        assertEquals("1", vo.getFileId());
        assertEquals("avatars/uuid.jpg", vo.getObjectKey());
        assertNotNull(vo.getAccessUrl());
    }

    @Test
    void shouldThrowWhenGetByObjectKeyNotFound() {
        when(fileMetadataMapper.selectOne(any())).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fileService.getByObjectKey("nonexistent-key"));
        assertEquals("文件不存在", ex.getMessage());
    }
}
