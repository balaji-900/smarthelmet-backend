package com.smarthelmet.helmet_backend.dto;

public class AlertRequest {
    private String helmetId;
    private String message;
    private Double lat;
    private Double lng;

    // Getters and Setters
    public String getHelmetId() {
        return helmetId;
    }
    public void setHelmetId(String helmetId) {
        this.helmetId = helmetId;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public Double getLat() {
        return lat;
    }
    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }
    public void setLng(Double lng) {
        this.lng = lng;
    }
}
