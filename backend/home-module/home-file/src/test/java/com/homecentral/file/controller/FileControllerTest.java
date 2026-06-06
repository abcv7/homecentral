package com.homecentral.file.controller;

import com.homecentral.file.api.vo.FileUploadVO;
import com.homecentral.file.service.IFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = FileController.class, properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.cloud.config.import-check.enabled=false"
})
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IFileService fileService;

    @Test
    void shouldUploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", "test-content".getBytes());

        FileUploadVO vo = new FileUploadVO();
        vo.setFileId("1");
        vo.setObjectKey("avatar/user_1/uuid.png");
        vo.setAccessUrl("http://localhost:9000/test/uuid.png");

        when(fileService.upload(any(), eq("avatar"), eq("user_1"), eq(1L))).thenReturn(vo);

        mockMvc.perform(multipart("/api/file/upload")
                        .file(file)
                        .param("businessType", "avatar")
                        .param("businessId", "user_1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.objectKey").value("avatar/user_1/uuid.png"));
    }

    @Test
    void shouldGetFileByObjectKey() throws Exception {
        FileUploadVO vo = new FileUploadVO();
        vo.setFileId("1");
        vo.setObjectKey("simple-key.jpg");

        when(fileService.getByObjectKey("simple-key.jpg")).thenReturn(vo);

        mockMvc.perform(get("/api/file/simple-key.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.objectKey").value("simple-key.jpg"));
    }
}
