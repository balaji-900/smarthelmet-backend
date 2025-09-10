package com.smarthelmet.helmet_backend.controller;

import com.smarthelmet.helmet_backend.model.Alert;
import com.smarthelmet.helmet_backend.model.SensorData;
import com.smarthelmet.helmet_backend.model.Worker;
import com.smarthelmet.helmet_backend.repository.SensorDataRepository;
import com.smarthelmet.helmet_backend.repository.WorkerRepository;
import com.smarthelmet.helmet_backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sensor-data")
public class SensorDataController {

    private final SensorDataRepository sensorDataRepository;
    private final WorkerRepository workerRepository;
    private final NotificationService notificationService;

    public SensorDataController(SensorDataRepository sensorDataRepository,
                                WorkerRepository workerRepository,
                                NotificationService notificationService) {
        this.sensorDataRepository = sensorDataRepository;
        this.workerRepository = workerRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/{helmetId}")
    public List<SensorData> getSensorDataByHelmetId(@PathVariable String helmetId) {
        return sensorDataRepository.findByHelmetId(helmetId);
    }

    @PostMapping
    public ResponseEntity<SensorData> createSensorData(@RequestBody SensorData sensorData) {
        SensorData savedData = sensorDataRepository.save(sensorData);

        // Check for alert
        if (sensorData.isAlert()) {
            Optional<Worker> workerOpt = workerRepository.findByHelmetId(sensorData.getHelmetId());
            workerOpt.ifPresent(worker -> {
                // Build Alert object
                Alert alert = new Alert();
                alert.setHelmetId(sensorData.getHelmetId());
                alert.setMessage("🚨 ALERT! Worker " + worker.getName() +
                        " (Helmet: " + worker.getHelmetId() + ") has triggered an emergency!");
                alert.setLat(sensorData.getLat());
                alert.setLng(sensorData.getLng());
                alert.setAlertType("sensor-triggered");
                alert.setTimestamp(LocalDateTime.now());

                // 🔴 Reuse NotificationService for SMS
                notificationService.sendAlertSms(worker, alert);
            });
        }

        return ResponseEntity.ok(savedData);
    }
}
