package com.homecentral.parcel.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 账户保存请求")
public class ApiAccountRequest {

    @Schema(description = "AppCode（阿里云快递查询）")
    private String apiKey;

    @Schema(description = "客户标识（可选）")
    private String customer;

    @Schema(description = "是否启用（false 时回退到全局 yml 配置）")
    private Boolean enabled;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
