package com.homecentral.parcel.scheduled;

import com.homecentral.parcel.service.IParcelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ParcelScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(ParcelScheduledTask.class);

    private final IParcelService parcelService;

    public ParcelScheduledTask(IParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshDaysAtStation() {
        log.info("开始刷新 days_at_station...");
        parcelService.refreshDaysAtStation();
        log.info("days_at_station 刷新完成");
    }
}
