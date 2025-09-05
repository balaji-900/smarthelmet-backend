package com.smarthelmet.helmet_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workers")
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // This is worker's own number (for login/records)
    @Column(unique = true, nullable = false)
    private String phoneNumber;

    // This will act as login password (instead of email-password, you use helmetId-password)
    @Column(unique = true, nullable = false)
    private String helmetId;

    // ✅ Add family contact (for SMS alerts)
    private String familyPhoneNumber;

    public Worker() {}

    public Worker(String name, String phoneNumber, String helmetId, String familyPhoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.helmetId = helmetId;
        this.familyPhoneNumber = familyPhoneNumber;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getHelmetId() { return helmetId; }
    public void setHelmetId(String helmetId) { this.helmetId = helmetId; }

    public String getFamilyPhoneNumber() { return familyPhoneNumber; }
    public void setFamilyPhoneNumber(String familyPhoneNumber) { this.familyPhoneNumber = familyPhoneNumber; }
}
