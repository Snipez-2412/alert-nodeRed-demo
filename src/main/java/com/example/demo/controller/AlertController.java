package com.example.demo.controller;

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

        Map<String, Object> processedPayload = new HashMap<>();
        processedPayload.put("message", message);
        processedPayload.put("timestamp", timestamp.toString());
        processedPayload.put("cameraName", cameraName);
        processedPayload.put("area", area);
        processedPayload.put("temperature", temperature);

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed."));
            }

            try {
                byte[] fileBytes = file.getBytes();
                String base64File = Base64.getEncoder().encodeToString(fileBytes);

                processedPayload.put("filename", file.getOriginalFilename());
                processedPayload.put("filetype", file.getContentType());
                processedPayload.put("filedata", base64File);
            } catch (IOException e) {
                return ResponseEntity.internalServerError().body(Map.of("error", "Failed to read uploaded image file."));
            }
        }

        // Send to Node-RED
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(processedPayload, headers);

        restTemplate.postForEntity(nodeRedUrl+"/receive-alert", request, String.class);

        return ResponseEntity.ok(processedPayload);
    }

}
