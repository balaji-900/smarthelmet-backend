package com.smarthelmet.helmet_backend.controller;

import com.smarthelmet.helmet_backend.model.SensorData;
import com.smarthelmet.helmet_backend.model.Alert;
import com.smarthelmet.helmet_backend.repository.SensorDataRepository;
import com.smarthelmet.helmet_backend.repository.AlertRepository;
import com.smarthelmet.helmet_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sensor-data")
public class SensorDataController {

    private final SensorDataRepository sensorDataRepository;
    private final AlertRepository alertRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
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
    public SensorData receiveData(@RequestBody SensorData data) {
        SensorData savedData = sensorDataRepository.save(data);

        if (data.isAlert()) {
            Alert alert = new Alert();
            alert.setHelmetId(data.getHelmetId());
            alert.setMessage("Sensor triggered alert");
            alert.setLat(data.getLat());
            alert.setLng(data.getLng());

            Alert savedAlert = alertRepository.save(alert);

            // Broadcast new alert
            messagingTemplate.convertAndSend("/topic/alerts", savedAlert);

            // Send notifications
            notificationService.notifyFamily(savedAlert);
            notificationService.notifyCoworkers(savedAlert);
        }

        return savedData;
    }
}
