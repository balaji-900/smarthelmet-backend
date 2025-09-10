package com.example.helmetbackend.controller;

import com.example.helmetbackend.model.Alert;
import com.example.helmetbackend.repository.AlertRepository;
import com.example.helmetbackend.service.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertRepository alertRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public AlertController(AlertRepository alertRepository,
                           NotificationService notificationService,
                           SimpMessagingTemplate messagingTemplate) {
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public String createAlert(@RequestBody Alert alert) {
        alert.setCreatedAt(LocalDateTime.now());
        Alert saved = alertRepository.save(alert);

        // notify
        notificationService.notifyFamily(saved);
        notificationService.notifyCoworkers(saved);

        // broadcast
        messagingTemplate.convertAndSend("/topic/alerts", saved);

        return "✅ Alert created and notifications sent.";
    }

    @PostMapping("/{id}/ack")
    public String acknowledgeAlert(@PathVariable Long id) {
        Optional<Alert> optional = alertRepository.findById(id);
        if (optional.isPresent()) {
            Alert alert = optional.get();
            alert.setAcknowledged(true);
            alert.setAcknowledgedAt(LocalDateTime.now());
            Alert saved = alertRepository.save(alert);

            // notify + broadcast updated alert
            notificationService.notifyCoworkers(saved);
            notificationService.notifyFamily(saved);
            messagingTemplate.convertAndSend("/topic/alerts", saved);

            return "✅ Alert acknowledged.";
        }
        return "❌ Alert not found.";
    }
}
