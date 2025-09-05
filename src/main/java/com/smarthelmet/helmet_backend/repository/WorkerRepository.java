package com.smarthelmet.helmet_backend.repository;

import com.smarthelmet.helmet_backend.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    Optional<Worker> findByHelmetId(String helmetId);
    Optional<Worker> findByPhoneNumber(String phoneNumber);
}