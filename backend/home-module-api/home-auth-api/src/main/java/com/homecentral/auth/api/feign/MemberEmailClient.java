package com.homecentral.auth.api.feign;

import com.homecentral.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "home-auth", contextId = "memberEmailClient")
public interface MemberEmailClient {

    @GetMapping("/api/auth/member/{id}/email")
    Result<String> getMemberEmail(@PathVariable("id") Long id);
}
