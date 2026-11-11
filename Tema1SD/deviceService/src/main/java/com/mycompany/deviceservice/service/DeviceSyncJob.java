package com.mycompany.deviceservice.service;

import com.mycompany.deviceservice.config.RabbitMQConfig;
import com.mycompany.deviceservice.model.Device;
import com.mycompany.deviceservice.repository.DeviceRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceSyncJob {

    private final DeviceRepository deviceRepository;
    private final RabbitTemplate rabbitTemplate;

    public DeviceSyncJob(DeviceRepository deviceRepository, RabbitTemplate rabbitTemplate) {
        this.deviceRepository = deviceRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedRate = 10000)
    public void syncDevices() {
        List<Device> devices = deviceRepository.findAll();
        System.out.println("Syncing " + devices.size() + " devices to simulator...");
        for (Device device : devices) {
            if (device.getId() != null) {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.ROUTING_KEY,
                        device
                );
            }
        }
    }
}
