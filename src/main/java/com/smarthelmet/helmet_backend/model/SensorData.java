package com.smarthelmet.helmet_backend.model;

//package com.smarthelmet.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "helmet_id")
    private String helmetId;

    @Column(name = "accel_x")
    private Double accelX;

    @Column(name = "accel_y")
    private Double accelY;

    @Column(name = "accel_z")
    private Double accelZ;

    @Column(name = "gas_level")
    private Double gasLevel;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;


    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "alert")
    private boolean alert;
}
