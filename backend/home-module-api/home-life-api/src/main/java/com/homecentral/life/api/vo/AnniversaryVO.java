package com.homecentral.life.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "纪念日视图")
public class AnniversaryVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "纪念日标题")
    private String title;

    @Schema(description = "纪念日日期")
    private LocalDate eventDate;

    @Schema(description = "提前提醒天数")
    private Integer remindBeforeDays;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
