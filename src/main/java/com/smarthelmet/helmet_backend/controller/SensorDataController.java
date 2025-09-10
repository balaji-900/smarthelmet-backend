package com.example.helmetbackend.controller;

import com.example.helmetbackend.model.Alert;
import com.example.helmetbackend.model.SensorData;
import com.example.helmetbackend.repository.AlertRepository;
import com.example.helmetbackend.repository.SensorDataRepository;
import com.example.helmetbackend.service.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/sensor-data")
public class SensorDataController {

    private final SensorDataRepository sensorDataRepository;
    private final AlertRepository alertRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public SensorDataController(SensorDataRepository sensorDataRepository,
                                AlertRepository alertRepository,
                                NotificationService notificationService,
                                SimpMessagingTemplate messagingTemplate) {
        this.sensorDataRepository = sensorDataRepository;
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public String addSensorData(@RequestBody SensorData data) {
        sensorDataRepository.save(data);

        if (data.isAlert()) {
            Alert alert = new Alert();
            alert.setHelmetId(data.getHelmetId());
            alert.setMessage("🚨 Sensor triggered alert");
            alert.setLat(data.getLat());
            alert.setLng(data.getLng());
            alert.setCreatedAt(LocalDateTime.now());

            Alert saved = alertRepository.save(alert);

            // notify
            notificationService.notifyFamily(saved);
            notificationService.notifyCoworkers(saved);

            // broadcast
            messagingTemplate.convertAndSend("/topic/alerts", saved);

            return "✅ Sensor data saved and alert created.";
        }
        return "ℹ️ Sensor data saved.";
    }
}
