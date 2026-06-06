package com.homecentral.parcel.event;

import org.springframework.context.ApplicationEvent;

import java.util.List;

public class ParcelImportedEvent extends ApplicationEvent {

    private final List<Long> successParcelIds;

    public ParcelImportedEvent(Object source, List<Long> successParcelIds) {
        super(source);
        this.successParcelIds = successParcelIds;
    }

    public List<Long> getSuccessParcelIds() {
        return successParcelIds;
    }
}
