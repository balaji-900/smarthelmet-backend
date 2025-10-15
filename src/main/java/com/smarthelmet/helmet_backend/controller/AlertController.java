package com.smarthelmet.helmet_backend.controller;

import com.smarthelmet.helmet_backend.dto.AlertRequest;
import com.smarthelmet.helmet_backend.model.Alert;
import com.smarthelmet.helmet_backend.model.Worker;
import com.smarthelmet.helmet_backend.repository.AlertRepository;
import com.smarthelmet.helmet_backend.repository.WorkerRepository;
import com.smarthelmet.helmet_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 🚨 Create new alert (triggered when a helmet detects a fall, gas leak, etc.)
    @PostMapping
    public ResponseEntity<?> sendAlert(@RequestBody AlertRequest alertRequest) {
        try {
            System.out.println("📥 Received alert: " + alertRequest);

            Optional<Worker> optionalWorker = workerRepository.findByHelmetId(alertRequest.getHelmetId());
            if (optionalWorker.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body("❌ Worker with helmetId " + alertRequest.getHelmetId() + " not found.");
            }

            Worker worker = optionalWorker.get();

            // Create new alert entity
            Alert alert = new Alert();
            alert.setHelmetId(alertRequest.getHelmetId());
            alert.setMessage(alertRequest.getMessage() != null ? alertRequest.getMessage() : "No message");
            alert.setAlertType(alertRequest.getAlertType() != null ? alertRequest.getAlertType() : "fall");
            alert.setLat(alertRequest.getLat());
            alert.setLng(alertRequest.getLng());
            alert.setTimestamp(LocalDateTime.now());
            alert.setAcknowledged(false);
            alertRepository.save(alert);

            System.out.println("✅ Alert saved for worker: " + worker.getName());

            // 🚨 Send SMS to family + schedule voice call
            notificationService.sendAlertSms(worker, alert);

            // 🚨 Send SMS to all other co-workers
            List<Worker> allWorkers = workerRepository.findAll();
            for (Worker w : allWorkers) {
                if (!w.getHelmetId().equals(worker.getHelmetId())) {
                    notificationService.sendAlertToWorker(w, alert);
                }
            }

            // 📡 WebSocket push (for dashboard updates)
            messagingTemplate.convertAndSend("/topic/alerts", alert);

            return ResponseEntity.ok("✅ Alert sent successfully. SMS and call scheduled.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .internalServerError()
                    .body("❌ Internal Server Error: " + e.getMessage());
        }
    }

    // 📜 Get all alerts history
    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        try {
            List<Alert> alerts = alertRepository.findAll();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🟢 Acknowledge alert (called when the worker is confirmed safe)
    @PostMapping("/{helmetId}/ack")
    public ResponseEntity<String> acknowledgeAlert(@PathVariable String helmetId) {
        try {
            List<Alert> alerts = alertRepository.findByHelmetId(helmetId);

            if (alerts.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body("❌ No alert found for helmetId: " + helmetId);
            }

            // Get the latest alert for that helmet
            Alert alert = alerts.get(alerts.size() - 1);
            alert.setAcknowledged(true);
            alert.setAcknowledgedAt(LocalDateTime.now());
            alertRepository.save(alert);

            Optional<Worker> optionalWorker = workerRepository.findByHelmetId(helmetId);
            if (optionalWorker.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body("⚠️ Worker not found for helmetId: " + helmetId);
            }

            Worker worker = optionalWorker.get();

            // Notify other workers that the alert is cleared
            List<Worker> allWorkers = workerRepository.findAll();
            for (Worker w : allWorkers) {
                if (!w.getHelmetId().equals(worker.getHelmetId())) {
                    notificationService.sendSafeSms(w, alert);
                }
            }

            // Notify the family of the worker
            notificationService.sendFamilySms(worker, alert);

            // WebSocket broadcast
            messagingTemplate.convertAndSend(
                    "/topic/alerts",
                    "✅ Worker " + worker.getName() + " (Helmet: " + worker.getHelmetId() + ") is SAFE."
            );

            return ResponseEntity.ok("✅ Alert acknowledged, all notifications sent.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .internalServerError()
                    .body("❌ Internal Server Error while acknowledging alert: " + e.getMessage());
        }
    }
}
