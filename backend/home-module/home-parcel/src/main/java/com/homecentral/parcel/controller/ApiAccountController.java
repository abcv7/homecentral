package com.homecentral.parcel.controller;

import com.homecentral.common.model.Result;
import com.homecentral.parcel.api.dto.ApiAccountRequest;
import com.homecentral.parcel.api.vo.ApiAccountVO;
import com.homecentral.parcel.service.IApiAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "API 账户", description = "用户级 API 凭据管理（如阿里云 AppCode）")
@RestController
@RequestMapping("/api/parcel/api-account")
public class ApiAccountController {

    private final IApiAccountService apiAccountService;

    public ApiAccountController(IApiAccountService apiAccountService) {
        this.apiAccountService = apiAccountService;
    }

    @Operation(summary = "查询用户的 API 凭据", description = "返回脱敏后的 AppCode（仅显示后 4 位）")
    @GetMapping("/{provider}")
    public Result<ApiAccountVO> get(@PathVariable String provider,
                                     @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.ok(null);
        return Result.ok(apiAccountService.get(userId, provider));
    }

    @Operation(summary = "保存/更新用户的 API 凭据", description = "Upsert 语义：不存在则创建，存在则更新")
    @PutMapping("/{provider}")
    public Result<ApiAccountVO> save(@PathVariable String provider,
                                      @RequestBody ApiAccountRequest request,
                                      @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.bizError("AUTH_401", "未登录");
        }
        return Result.ok(apiAccountService.save(userId, provider, request));
    }

    @Operation(summary = "删除用户的 API 凭据", description = "删除后将回退到全局 yml 配置")
    @DeleteMapping("/{provider}")
    public Result<Void> delete(@PathVariable String provider,
                                @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.bizError("AUTH_401", "未登录");
        }
        apiAccountService.delete(userId, provider);
        return Result.ok(null);
    }
}
