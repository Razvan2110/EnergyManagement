package com.mycompany.monitoringservice.service;

import com.mycompany.monitoringservice.entity.HourlyConsumption;
import com.mycompany.monitoringservice.entity.SensorMeasurement;
import com.mycompany.monitoringservice.repository.HourlyConsumptionRepository;
import com.mycompany.monitoringservice.repository.SensorMeasurementRepository;
import com.mycompany.monitoringservice.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsumptionService {

    private static final Logger log = LoggerFactory.getLogger(ConsumptionService.class);

    private final SensorMeasurementRepository measurementRepository;
    private final HourlyConsumptionRepository hourlyConsumptionRepository;
    private final RabbitTemplate rabbitTemplate;

    public ConsumptionService(SensorMeasurementRepository measurementRepository, HourlyConsumptionRepository hourlyConsumptionRepository, RabbitTemplate rabbitTemplate) {
        this.measurementRepository = measurementRepository;
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

   
    @Scheduled(cron = "*/30 * * * * *")
    @Transactional
    public void computeHourlyConsumption() {
        log.info("Starting hourly consumption computation job...");

        long now = Instant.now().toEpochMilli();
        long oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS).toEpochMilli();

        List<SensorMeasurement> measurements = measurementRepository.findByTimestampBetween(oneHourAgo, now);

        if (measurements.isEmpty()) {
            log.info("No measurements found for the last hour.");
            return;
        }

        Map<String, Double> consumptionByDevice = measurements.stream()
                .collect(Collectors.groupingBy(
                        SensorMeasurement::getDeviceId,
                        Collectors.summingDouble(SensorMeasurement::getMeasurementValue)
                ));

        consumptionByDevice.forEach((deviceId, totalConsumption) -> {
            HourlyConsumption hourly = new HourlyConsumption(
                    now,
                    deviceId,
                    totalConsumption
            );

            hourlyConsumptionRepository.save(hourly);
            log.info("Saved hourly consumption for device {}: {}", deviceId, totalConsumption);

            if (totalConsumption > 1) {
                 // " device <<id>> exceeded consumption limit .now it has <<consumed kWh>> ."
                String message = "device " + deviceId + " exceeded consumption limit .now it has " + totalConsumption + " .";
                rabbitTemplate.convertAndSend(RabbitMQConfig.OVERCONSUMPTION_QUEUE_NAME, message);
                log.warn("Overconsumption alert sent for device {}: {}", deviceId, totalConsumption);
            }
        });

        log.info("Hourly consumption computation finished.");
    }

    public List<HourlyConsumption> getHourlyConsumption(String deviceId) {
        return hourlyConsumptionRepository.findByDeviceId(deviceId);
    }

    public List<HourlyConsumption> getHourlyConsumptionForDay(String deviceId, java.time.LocalDate date) {
        java.time.LocalDateTime startOfDay = date.atStartOfDay();
        java.time.LocalDateTime endOfDay = date.atTime(java.time.LocalTime.MAX);

        long startTimestamp = startOfDay.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
        long endTimestamp = endOfDay.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();

        return hourlyConsumptionRepository.findByDeviceIdAndTimestampBetween(deviceId, startTimestamp, endTimestamp);
    }
    
    public List<HourlyConsumption> getAllHourlyConsumptions() {
        return hourlyConsumptionRepository.findAll();
    }
}
