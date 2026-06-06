package com.homecentral.parcel.tracking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecentral.parcel.api.vo.TrackingVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AliyunExpressService {

    private static final Logger log = LoggerFactory.getLogger(AliyunExpressService.class);

    private static final String BASE_URL = "https://jmexpresv2.market.alicloudapi.com";

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId TZ_SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String appcode;

    public AliyunExpressService(RestTemplate restTemplate, ObjectMapper objectMapper,
                                 @Value("${app.aliyun.express.appcode:}") String appcode) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.appcode = appcode;
    }

    public TrackingVO autoQuery(String trackingNumber, String phone) {
        return queryWithDiscern(trackingNumber, phone);
    }

    public TrackingVO queryByNumber(String trackingNumber, String courierCode, String phone) {
        return doQuery(trackingNumber, courierCode, phone);
    }

    public TrackingVO queryWithDiscern(String trackingNumber, String phone) {
        try {
            String expressCode = discernExpressCode(trackingNumber);
            if (expressCode != null) {
                log.info("Discerned {} -> courier={}", trackingNumber, expressCode);
                return doQuery(trackingNumber, expressCode, phone);
            }
            log.warn("Discern failed for {}, fallback to auto-recognize", trackingNumber);
        } catch (Exception e) {
            log.warn("Discern error for {}, fallback to auto-recognize", trackingNumber, e);
        }
        return doQuery(trackingNumber, null, phone);
    }

    private String discernExpressCode(String trackingNumber) {
        JsonNode root = callAliyunApiRaw("/express/number-discern", trackingNumber, null, null);
        if (root == null) return null;
        int code = root.get("code").asInt();
        if (code != 200) return null;
        JsonNode data = root.get("data");
        if (data == null || data.isNull()) return null;
        JsonNode details = data.get("details");
        if (details == null || !details.isArray() || details.isEmpty()) return null;
        JsonNode first = details.get(0);
        if (first == null || !first.has("expressCode")) return null;
        return first.get("expressCode").asText();
    }

    private TrackingVO doQuery(String trackingNumber, String courierCode, String phone) {
        TrackingVO vo = new TrackingVO();
        vo.setTrackingNumber(trackingNumber);
        try {
            JsonNode data = callAliyunApi("/express/query-v2", trackingNumber, courierCode, phone);
            if (data == null) {
                vo.setMessage("无数据");
                vo.setState("unknown");
                vo.setStateLabel("无数据");
                vo.setTracks(List.of());
                return vo;
            }

            vo.setCourierCode(data.has("expressCode") ? data.get("expressCode").asText() : "");
            vo.setCourierName(data.has("expressCompanyName") ? data.get("expressCompanyName").asText() : "");
            vo.setState(data.has("logisticsStatus") ? data.get("logisticsStatus").asText() : "");
            vo.setStateLabel(data.has("logisticsStatusDesc") ? data.get("logisticsStatusDesc").asText() : "");
            vo.setMessage("ok");

            JsonNode details = data.get("logisticsTraceDetails");
            if (details != null && details.isArray() && !details.isEmpty()) {
                List<TrackingVO.TrackItem> tracks = new ArrayList<>();
                for (JsonNode item : details) {
                    TrackingVO.TrackItem ti = new TrackingVO.TrackItem();
                    if (item.has("time") && !item.get("time").isNull()) {
                        ti.setTime(Instant.ofEpochMilli(item.get("time").asLong())
                            .atZone(TZ_SHANGHAI).format(DT_FMT));
                    }
                    ti.setContext(item.has("desc") ? item.get("desc").asText() : null);
                    tracks.add(ti);
                }
                vo.setTracks(tracks);
            } else {
                vo.setTracks(List.of());
            }
        } catch (Exception e) {
            log.error("AliyunAPI query failed for {}", trackingNumber, e);
            vo.setMessage(e.getMessage());
            vo.setState("error");
            vo.setStateLabel("查询失败");
            vo.setTracks(List.of());
        }
        return vo;
    }

    public JsonNode numberDiscern(String trackingNumber) {
        return callAliyunApiRaw("/express/number-discern", trackingNumber, null, null);
    }

    public JsonNode queryOutlets(String shipperCode, String provinceName, String cityName,
                                  String areaName, String address) {
        try {
            HttpHeaders headers = buildHeaders();
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("shipperCode", shipperCode);
            body.add("provinceName", provinceName);
            body.add("cityName", cityName);
            body.add("areaName", areaName);
            if (address != null && !address.isEmpty()) {
                body.add("address", address);
            }

            ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/express-delivery-outlets/query-v2",
                HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

            String resp = response.getBody();
            if (resp == null || resp.isEmpty()) return null;
            return objectMapper.readTree(resp);
        } catch (Exception e) {
            log.error("AliyunAPI queryOutlets failed", e);
            return null;
        }
    }

    private JsonNode callAliyunApi(String path, String trackingNumber, String courierCode, String phone) {
        JsonNode root = callAliyunApiRaw(path, trackingNumber, courierCode, phone);
        if (root == null) return null;
        int code = root.get("code").asInt();
        if (code != 200) {
            log.warn("AliyunAPI {} error: code={}, msg={}", trackingNumber, code,
                     root.has("msg") ? root.get("msg").asText() : "");
            return null;
        }
        JsonNode data = root.get("data");
        if (data == null || data.isNull()) return null;
        return data;
    }

    private JsonNode callAliyunApiRaw(String path, String trackingNumber, String courierCode, String phone) {
        try {
            HttpHeaders headers = buildHeaders();
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("number", trackingNumber);
            body.add("sort", "desc");
            if (courierCode != null && !courierCode.isEmpty()) {
                body.add("expressCode", courierCode);
            }
            if (phone != null && !phone.isEmpty()) {
                body.add("mobile", phone);
            }

            ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + path, HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);

            String resp = response.getBody();
            log.info("AliyunAPI {} {}: HTTP {}, body={}", path, trackingNumber,
                     response.getStatusCode().value(),
                     resp != null ? resp.substring(0, Math.min(resp.length(), 500)) : "null");

            if (resp == null || resp.isEmpty()) return null;
            return objectMapper.readTree(resp);
        } catch (Exception e) {
            log.error("AliyunAPI {} failed for {}", path, trackingNumber, e);
            return null;
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "APPCODE " + appcode);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }
}
