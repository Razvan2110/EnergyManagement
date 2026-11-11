package com.mycompany.monitoringservice.repository;

import com.mycompany.monitoringservice.entity.SensorMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorMeasurementRepository extends JpaRepository<SensorMeasurement, Long> {
    List<SensorMeasurement> findByTimestampBetween(Long start, Long end);
}
