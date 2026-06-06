package com.homecentral.parcel.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "物流轨迹视图")
public class TrackingVO {

    @Schema(description = "快递单号")
    private String trackingNumber;

    @Schema(description = "快递公司编码")
    private String courierCode;

    @Schema(description = "快递公司名称")
    private String courierName;

    @Schema(description = "物流状态")
    private String state;

    @Schema(description = "状态标签")
    private String stateLabel;

    @Schema(description = "状态描述信息")
    private String message;

    @Schema(description = "物流轨迹列表")
    private List<TrackItem> tracks;

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCourierCode() {
        return courierCode;
    }

    public void setCourierCode(String courierCode) {
        this.courierCode = courierCode;
    }

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateLabel() {
        return stateLabel;
    }

    public void setStateLabel(String stateLabel) {
        this.stateLabel = stateLabel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<TrackItem> getTracks() {
        return tracks;
    }

    public void setTracks(List<TrackItem> tracks) {
        this.tracks = tracks;
    }

    public static class TrackItem {

        @Schema(description = "时间")
        private String time;

        @Schema(description = "轨迹内容")
        private String context;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }
    }
}
