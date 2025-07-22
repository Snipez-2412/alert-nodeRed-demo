package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AlertController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${nodeRed.alert.url}")
    private String alertUrl;

    @PostMapping("/trigger-alert")
    public ResponseEntity<Map<String, Object>> triggerAlert(@RequestBody Map<String, Object> payload) {
        String message = (String) payload.get("message");
        String timestampStr = (String) payload.get("timestamp");
        String cameraName = (String) payload.get("cameraName");
        String area = (String) payload.get("area");
        String temperature = (String) payload.get("temperature");

        // Manually parse the timestamp
        Instant timestamp;
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            timestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid timestamp format. Use yyyy-MM-dd HH:mm:ss"));
        }

        Map<String, Object> processedPayload = new HashMap<>();
        processedPayload.put("message", message);
        processedPayload.put("timestamp", timestamp.toString());
        processedPayload.put("cameraName", cameraName);
        processedPayload.put("area", area);
        processedPayload.put("temperature", temperature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(processedPayload, headers);

        restTemplate.postForEntity(alertUrl + "/receive-alert", request, String.class);

        return ResponseEntity.ok(processedPayload);
    }

}
