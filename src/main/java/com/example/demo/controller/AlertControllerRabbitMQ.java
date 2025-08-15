package com.example.demo.controller;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.entity.AlertEntity;
import com.example.demo.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AlertControllerRabbitMQ {

    @Autowired
    private AlertService alertService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping(value = "/call-rabbit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

        String eventId = alertService.generateEventId();

        AlertEntity alert = new AlertEntity();
        alert.setEventId(eventId);
        alert.setMessage(message);
        alert.setTimestamp(timestamp.toString());
        alert.setCameraName(cameraName);
        alert.setArea(area);
        alert.setTemperature(temperature);

        if (file != null && !file.isEmpty()) {
            try {
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed."));
                }
                byte[] fileBytes = file.getBytes();
                String base64File = Base64.getEncoder().encodeToString(fileBytes);

                alert.setFilename(file.getOriginalFilename());
                alert.setFiletype(contentType);
                alert.setFiledata(base64File);
            } catch (IOException e) {
                return ResponseEntity.internalServerError().body(Map.of("error", "Failed to read uploaded image file."));
            }
        }

        // Save to DB
        alertService.saveAlert(alert);

        // Publish to RabbitMQ instead of Node-RED
        rabbitTemplate.convertAndSend(RabbitMQConfig.ALERT_QUEUE, alert);

        return ResponseEntity.ok(Map.of(
                "eventId", eventId,
                "message", message,
                "timestamp", timestamp.toString(),
                "cameraName", cameraName,
                "area", area,
                "temperature", temperature
        ));
    }
}