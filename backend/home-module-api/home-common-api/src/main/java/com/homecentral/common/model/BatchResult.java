package com.homecentral.common.model;

import java.util.ArrayList;
import java.util.List;

public class BatchResult<T> {

    private int totalCount;
    private int successCount;
    private int failureCount;
    private List<T> successItems;
    private List<FailureItem> failureItems;

    public static class FailureItem {
        private int index;
        private String item;
        private String reason;

        public FailureItem() {}

        public FailureItem(int index, String item, String reason) {
            this.index = index;
            this.item = item;
            this.reason = reason;
        }

        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        public String getItem() { return item; }
        public void setItem(String item) { this.item = item; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    private BatchResult() {}

    public static <T> BatchResult<T> of(List<T> success, List<FailureItem> failures) {
        BatchResult<T> r = new BatchResult<>();
        r.successItems = success != null ? success : new ArrayList<>();
        r.failureItems = failures != null ? failures : new ArrayList<>();
        r.successCount = r.successItems.size();
        r.failureCount = r.failureItems.size();
        r.totalCount = r.successCount + r.failureCount;
        return r;
    }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
    public List<T> getSuccessItems() { return successItems; }
    public void setSuccessItems(List<T> successItems) { this.successItems = successItems; }
    public List<FailureItem> getFailureItems() { return failureItems; }
    public void setFailureItems(List<FailureItem> failureItems) { this.failureItems = failureItems; }
}
