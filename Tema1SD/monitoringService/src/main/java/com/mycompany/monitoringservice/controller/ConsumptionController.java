package com.mycompany.monitoringservice.controller;

import com.mycompany.monitoringservice.entity.HourlyConsumption;
import com.mycompany.monitoringservice.service.ConsumptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hourlyConsumption")
@CrossOrigin(origins = "*")
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    public ConsumptionController(ConsumptionService consumptionService) {
        this.consumptionService = consumptionService;
    }

    @GetMapping
    public ResponseEntity<List<HourlyConsumption>> getAll() {
        return ResponseEntity.ok(consumptionService.getAllHourlyConsumptions());
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<List<HourlyConsumption>> getByDevice(
            @PathVariable String deviceId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date
    ) {
        if (date != null) {
            return ResponseEntity.ok(consumptionService.getHourlyConsumptionForDay(deviceId, date));
        }
        return ResponseEntity.ok(consumptionService.getHourlyConsumption(deviceId));
    }
}
