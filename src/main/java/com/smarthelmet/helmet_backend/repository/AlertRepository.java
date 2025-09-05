package com.smarthelmet.helmet_backend.repository;

import com.smarthelmet.helmet_backend.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByHelmetId(String helmetId);
}
