package com.smarthelmet.helmet_backend.controller;

import com.smarthelmet.helmet_backend.model.Worker;
import com.smarthelmet.helmet_backend.repository.WorkerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final WorkerRepository workerRepository;

    public AuthController(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    // SIGNUP
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody Worker worker) {
        // Check if helmetId or phone already exists
        if (workerRepository.findByHelmetId(worker.getHelmetId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Helmet ID already registered"));
        }
        if (workerRepository.findByPhoneNumber(worker.getPhoneNumber()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Phone number already registered"));
        }

        Worker saved = workerRepository.save(worker);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Signup successful",
                "worker", saved
        ));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String helmetId = request.get("helmetId");

        if (helmetId == null || helmetId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "helmetId is required"));
        }

        return workerRepository.findByHelmetId(helmetId)
                .map(worker -> ResponseEntity.ok(Map.of(
                        "message", "Login successful",
                        "worker", worker
                )))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid helmetId")));
    }
}
