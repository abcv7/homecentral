package com.homecentral.file.controller;

import com.homecentral.common.model.Result;
import com.homecentral.file.api.vo.FileUploadVO;
import com.homecentral.file.service.IFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件管理", description = "文件上传和查询")
@RestController
@RequestMapping("/api/file")
public class FileController {

    private final IFileService fileService;

    public FileController(IFileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "文件上传", description = "上传文件到MinIO对象存储")
    @PostMapping("/upload")
    public Result<FileUploadVO> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "businessType", required = false, defaultValue = "default") String businessType,
                                       @RequestParam(value = "businessId", required = false) String businessId,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(fileService.upload(file, businessType, businessId, userId));
    }

    @Operation(summary = "按ObjectKey查询", description = "根据ObjectKey获取文件信息")
    @GetMapping("/{objectKey}")
    public Result<FileUploadVO> getByObjectKey(@PathVariable String objectKey) {
        return Result.ok(fileService.getByObjectKey(objectKey));
    }
}
