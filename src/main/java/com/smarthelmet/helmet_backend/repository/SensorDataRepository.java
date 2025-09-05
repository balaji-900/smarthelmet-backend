package com.smarthelmet.helmet_backend.repository;

import com.smarthelmet.helmet_backend.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    List<SensorData> findByHelmetId(String helmetId);
}
