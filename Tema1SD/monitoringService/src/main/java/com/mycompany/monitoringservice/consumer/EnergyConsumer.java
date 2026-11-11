package com.mycompany.monitoringservice.consumer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mycompany.monitoringservice.entity.SensorMeasurement;
import com.mycompany.monitoringservice.repository.SensorMeasurementRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class EnergyConsumer {

    private static final Logger log = LoggerFactory.getLogger(EnergyConsumer.class);

    private final SensorMeasurementRepository measurementRepository;

    public EnergyConsumer(SensorMeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    @RabbitListener(queues = "simulator_to_monitoring")
    public void consumeMessage(MeasurementPayload payload) {
        log.info("Received measurement: {}", payload);

        try {
            SensorMeasurement measurement = new SensorMeasurement(
                    payload.getTimestamp(),
                    payload.getDeviceId(),
                    payload.getMeasurementValue()
            );

            measurementRepository.save(measurement);
            log.info("Saved measurement to DB: {}", measurement);
        } catch (Exception e) {
            log.error("Error saving measurement: {}", e.getMessage());
        }
    }

    public static class MeasurementPayload {
        private Long timestamp;
        @JsonProperty("device_id")
        private String deviceId;
        @JsonProperty("measurement_value")
        private Double measurementValue;

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

        public Double getMeasurementValue() {
            return measurementValue;
        }

        public void setMeasurementValue(Double measurementValue) {
            this.measurementValue = measurementValue;
        }

        @Override
        public String toString() {
            return "MeasurementPayload{" +
                    "timestamp=" + timestamp +
                    ", deviceId='" + deviceId + '\'' +
                    ", measurementValue=" + measurementValue +
                    '}';
        }
    }
}
