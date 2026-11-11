package com.mycompany.monitoringservice.repository;

import com.mycompany.monitoringservice.entity.HourlyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, Long> {
    List<HourlyConsumption> findByDeviceId(String deviceId);
    List<HourlyConsumption> findByDeviceIdAndTimestampBetween(String deviceId, Long start, Long end);
}
