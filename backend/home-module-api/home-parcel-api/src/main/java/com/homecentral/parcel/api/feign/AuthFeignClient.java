package com.homecentral.parcel.api.feign;

import com.homecentral.auth.api.vo.MemberVO;
import com.homecentral.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "home-auth", contextId = "parcelAuthFeignClient")
public interface AuthFeignClient {

    @GetMapping("/api/auth/member/{id}")
    Result<MemberVO> getMemberById(@PathVariable("id") Long id);
}
