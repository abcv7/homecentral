package com.homecentral.life.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Schema(description = "创建纪念日请求")
public class AnniversaryCreateRequest {

    @Schema(description = "纪念日标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "结婚纪念日")
    @NotBlank
    private String title;

    @Schema(description = "纪念日日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-12-25")
    @FutureOrPresent
    private LocalDate eventDate;

    @Schema(description = "提前提醒天数", example = "7")
    private Integer remindBeforeDays;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public Integer getRemindBeforeDays() {
        return remindBeforeDays;
    }

    public void setRemindBeforeDays(Integer remindBeforeDays) {
        this.remindBeforeDays = remindBeforeDays;
    }
}
