package com.mycompany.monitoringservice.entity;

import jakarta.persistence.*;

@Entity
public class HourlyConsumption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long timestamp;
    private String deviceId;
    private Double totalConsumption;

    public HourlyConsumption() {
    }

    public HourlyConsumption(Long timestamp, String deviceId, Double totalConsumption) {
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.totalConsumption = totalConsumption;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Double getTotalConsumption() {
        return totalConsumption;
    }

    public void setTotalConsumption(Double totalConsumption) {
        this.totalConsumption = totalConsumption;
    }
}
