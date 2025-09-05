package com.smarthelmet.helmet_backend.controller;

import com.smarthelmet.helmet_backend.model.Worker;
import com.smarthelmet.helmet_backend.repository.WorkerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/workers")
public class WorkerController {

    private final WorkerRepository workerRepository;

    public WorkerController(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    // ✅ Get all workers
    @GetMapping
    public List<Worker> getAllWorkers() {
        return workerRepository.findAll();
    }

    // ✅ Register new worker (Signup)
    @PostMapping("/register")
    public ResponseEntity<?> createWorker(@RequestBody Worker worker) {
        // Check duplicate phone
        if (workerRepository.findByPhoneNumber(worker.getPhoneNumber()).isPresent()) {
            return ResponseEntity.badRequest().body("Worker already exists with this phone number!");
        }
        Worker saved = workerRepository.save(worker);
        return ResponseEntity.ok(saved);
    }

    // ✅ Login using phone + helmetId
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Worker loginRequest) {
        Optional<Worker> existingWorker = workerRepository.findByPhoneNumber(loginRequest.getPhoneNumber());

        if (existingWorker.isPresent()) {
            Worker worker = existingWorker.get();
            if (worker.getHelmetId().equals(loginRequest.getHelmetId())) {
                return ResponseEntity.ok(worker);
            }
        }
        return ResponseEntity.status(401).body("Invalid phone number or helmetId!");
    }
}

