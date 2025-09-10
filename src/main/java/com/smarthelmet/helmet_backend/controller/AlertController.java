package com.smarthelmet.helmet_backend.controller;

import com.smarthelmet.helmet_backend.model.Alert;
import com.smarthelmet.helmet_backend.repository.AlertRepository;
import com.smarthelmet.helmet_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertRepository alertRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public AlertController(AlertRepository alertRepository,
                           NotificationService notificationService,
                           SimpMessagingTemplate messagingTemplate) {
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    @PostMapping("/{id}/ack")
    public Alert acknowledgeAlert(@PathVariable Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setAcknowledged(true);
        alert.setAcknowledgedAt(LocalDateTime.now());
        Alert saved = alertRepository.save(alert);

        // Broadcast updated alert
        messagingTemplate.convertAndSend("/topic/alerts", saved);

        return saved;
    }
}
