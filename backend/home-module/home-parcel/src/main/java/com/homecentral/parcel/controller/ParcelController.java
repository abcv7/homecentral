package com.homecentral.parcel.controller;

import com.homecentral.auth.api.vo.MemberVO;
import com.homecentral.common.model.Result;
import com.homecentral.parcel.api.feign.AuthFeignClient;
import com.homecentral.parcel.api.dto.ParcelCreateRequest;
import com.homecentral.parcel.api.dto.ParcelUpdateRequest;
import com.homecentral.parcel.api.vo.ParcelSummaryVO;
import com.homecentral.parcel.api.vo.SharedParcelUserVO;
import com.homecentral.parcel.api.vo.TrackingVO;
import com.homecentral.parcel.service.IParcelService;
import com.homecentral.parcel.tracking.AliyunExpressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "快递管理", description = "快递CRUD、取件收货、分享、物流查询")
@RestController
@RequestMapping("/api/parcel")
public class ParcelController {

    private final IParcelService parcelService;
    private final AliyunExpressService aliyunExpressService;
    private final AuthFeignClient authFeignClient;

    public ParcelController(IParcelService parcelService,
                            AliyunExpressService aliyunExpressService,
                            AuthFeignClient authFeignClient) {
        this.parcelService = parcelService;
        this.aliyunExpressService = aliyunExpressService;
        this.authFeignClient = authFeignClient;
    }

    @Operation(summary = "创建快递", description = "手动录入快递信息")
    @PostMapping
    public Result<ParcelSummaryVO> create(@Valid @RequestBody ParcelCreateRequest request,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(parcelService.create(request, userId));
    }

    @Operation(summary = "按ID查询快递", description = "根据快递ID获取详情")
    @GetMapping("/{id}")
    public Result<ParcelSummaryVO> getById(@PathVariable Long id) {
        return Result.ok(parcelService.getById(id));
    }

    @Operation(summary = "分页列表查询", description = "分页查询快递列表，支持状态和单号筛选")
    @GetMapping
    public Result<Page<ParcelSummaryVO>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                               @RequestParam(required = false) String status,
                                               @RequestParam(required = false) String trackingNumber,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return Result.page(parcelService.list(userId, status, trackingNumber, page, size));
    }

    @Operation(summary = "更新快递", description = "更新快递信息（部分字段）")
    @PutMapping("/{id}")
    public Result<ParcelSummaryVO> update(@PathVariable Long id,
                                           @RequestBody ParcelUpdateRequest request,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(parcelService.update(id, request, userId));
    }

    @Operation(summary = "取件操作", description = "将快递状态从待取件改为已取件")
    @PutMapping("/{id}/pickup")
    public Result<ParcelSummaryVO> pickUp(@PathVariable Long id,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(parcelService.pickUp(id, userId));
    }

    @Operation(summary = "收货确认", description = "将快递状态从未取件改为已收货")
    @PutMapping("/{id}/receive")
    public Result<ParcelSummaryVO> receive(@PathVariable Long id,
                                            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(parcelService.receive(id, userId));
    }

    @Operation(summary = "软删除快递", description = "仅可删除已收货的快递")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        parcelService.delete(id, userId);
        return Result.ok(null);
    }

    @Operation(summary = "分享快递", description = "将快递分享给指定用户")
    @PostMapping("/{id}/share")
    public Result<Void> share(@PathVariable Long id,
                               @RequestParam Long targetUserId,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        parcelService.shareParcel(id, targetUserId, userId);
        return Result.ok(null);
    }

    @Operation(summary = "取消分享", description = "取消对指定用户的快递分享")
    @DeleteMapping("/{id}/share")
    public Result<Void> unshare(@PathVariable Long id,
                                 @RequestParam Long targetUserId,
                                 @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        parcelService.unshareParcel(id, targetUserId, userId);
        return Result.ok(null);
    }

    @Operation(summary = "查询已分享用户", description = "查询此快递已分享给哪些用户（含昵称/邮箱）")
    @GetMapping("/{id}/shares")
    public Result<List<SharedParcelUserVO>> listShares(@PathVariable Long id,
                                                        @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(parcelService.listSharedUsers(id, userId));
    }

    @Operation(summary = "查询物流轨迹", description = "根据快递ID查询物流轨迹")
    @GetMapping("/{id}/tracking")
    public Result<TrackingVO> queryTracking(@PathVariable Long id) {
        return Result.ok(parcelService.queryTracking(id));
    }

    @Operation(summary = "自动识别查询", description = "自动识别快递公司并查询物流信息")
    @GetMapping("/tracking/auto-com")
    public Result<TrackingVO> autoComplete(@RequestParam String trackingNumber,
                                            @RequestParam(required = false) String phone,
                                            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.ok(aliyunExpressService.queryWithDiscern(trackingNumber, phone, userId));
    }

    @Operation(summary = "按单号查询", description = "指定快递公司查询物流轨迹")
    @GetMapping("/tracking/query")
    public Result<TrackingVO> queryTrackingByNumber(@RequestParam String trackingNumber,
                                                     @RequestParam(required = false) String courierCode,
                                                     @RequestParam(required = false) String phone,
                                                     @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        TrackingVO vo = courierCode != null && !courierCode.isBlank()
            ? aliyunExpressService.queryByNumber(trackingNumber, courierCode, phone, userId)
            : aliyunExpressService.queryWithDiscern(trackingNumber, phone, userId);
        return Result.ok(vo);
    }

    @Operation(summary = "获取手机号后四位", description = "根据当前用户ID获取手机号后四位（用于快递物流查询）")
    @GetMapping("/phone-tail")
    public Result<String> getPhoneTail(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return Result.ok(null);
        try {
            Result<MemberVO> memberResult = authFeignClient.getMemberById(userId);
            if (memberResult != null && memberResult.getData() != null && memberResult.getData().getPhone() != null) {
                String phone = memberResult.getData().getPhone();
                if (phone.length() >= 4) {
                    return Result.ok(phone.substring(phone.length() - 4));
                }
            }
        } catch (Exception e) {
        }
        return Result.ok(null);
    }
}
