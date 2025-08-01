package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alert")
public class ReceiveAlert {

    @PostMapping("/stage1")
    public ResponseEntity<String> receiveStage1Alert(@RequestBody Map<String, Object> payload) {
        String message = (String) payload.getOrDefault("message", "No message provided");
        String timestamp = (String) payload.getOrDefault("timestamp", "unknown");
        String area = (String) payload.getOrDefault("area", "unknown");

        System.out.println("Stage 1 Alert Received:");
        System.out.println("Message: " + message);
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Area: " + area);

        return ResponseEntity.ok("Stage 1 alert received successfully.");
    }
}
