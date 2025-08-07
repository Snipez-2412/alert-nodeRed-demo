package com.example.demo.service;

import com.example.demo.entity.AlertEntity;
import com.example.demo.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    public String generateEventId() {
        int counter = 1;
        String eventId;
        do {
            eventId = String.format("EV%02d", counter);
            Optional<AlertEntity> existing = alertRepository.findById(eventId);
            if (existing.isEmpty()) break;
            counter++;
        } while (true);
        return eventId;
    }

    public AlertEntity saveAlert(AlertEntity alert) {
        return alertRepository.save(alert);
    }
}