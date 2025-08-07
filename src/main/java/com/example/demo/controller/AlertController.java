package com.example.demo.controller;

import com.example.demo.entity.AlertEntity;
import com.example.demo.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AlertController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private AlertService alertService;

    @Value("${nodeRed.alert.url}")
    private String nodeRedUrl;

    @PostMapping(value = "/trigger-alert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> triggerAlert(
            @RequestPart("message") String message,
            @RequestPart("timestamp") String timestampStr,
            @RequestPart("cameraName") String cameraName,
            @RequestPart("area") String area,
            @RequestPart("temperature") String temperature,
            @RequestPart(value = "image", required = false) MultipartFile file
    ) {
        // Parse timestamp
        Instant timestamp;
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            timestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid timestamp format. Use yyyy-MM-dd HH:mm:ss"));
        }

        // Generate eventId
        String eventId = alertService.generateEventId();

        AlertEntity alert = new AlertEntity();
        alert.setEventId(eventId);
        alert.setMessage(message);
        alert.setTimestamp(timestamp.toString());
        alert.setCameraName(cameraName);
        alert.setArea(area);
        alert.setTemperature(temperature);

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed."));
            }

            try {
                byte[] fileBytes = file.getBytes();
                String base64File = Base64.getEncoder().encodeToString(fileBytes);

                alert.setFilename(file.getOriginalFilename());
                alert.setFiletype(contentType);
                alert.setFiledata(base64File);

            } catch (IOException e) {
                return ResponseEntity.internalServerError().body(Map.of("error", "Failed to read uploaded image file."));
            }
        }

        alertService.saveAlert(alert);

        // Send to Node-RED
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AlertEntity> request = new HttpEntity<>(alert, headers);
        restTemplate.postForEntity(nodeRedUrl + "/receive-alert", request, String.class);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("eventId", eventId);
        responseMap.put("message", message);
        responseMap.put("timestamp", timestamp.toString());
        responseMap.put("cameraName", cameraName);
        responseMap.put("area", area);
        responseMap.put("temperature", temperature);

        if (file != null && !file.isEmpty()) {
            responseMap.put("filename", file.getOriginalFilename());
            responseMap.put("filetype", file.getContentType());
        }

        return ResponseEntity.ok(responseMap);
    }
}