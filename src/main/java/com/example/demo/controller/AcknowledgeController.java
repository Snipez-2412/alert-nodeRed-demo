package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AcknowledgeController {

    @Value("${nodeRed.alert.url}")
    private String nodeRedUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/acknowledge")
    public ResponseEntity<String> sendAcknowledge(@RequestBody Map<String, Object> request) {
        try {
            restTemplate.postForEntity(nodeRedUrl + "/acknowledge", request, String.class);
            return ResponseEntity.ok("Acknowledgement sent to Node-RED.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending acknowledgement: " + e.getMessage());
        }
    }
}
