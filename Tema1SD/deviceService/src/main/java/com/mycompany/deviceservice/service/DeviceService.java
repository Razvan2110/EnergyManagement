package com.mycompany.deviceservice.service;


import com.mycompany.deviceservice.dto.UpdateDeviceRequest;
import com.mycompany.deviceservice.model.Device;
import com.mycompany.deviceservice.repository.DeviceRepository;
import com.mycompany.deviceservice.dto.DeviceDto;
import com.mycompany.deviceservice.dto.CreateDeviceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }


    public DeviceDto createDevice(CreateDeviceRequest request) {
        Device device = new Device(null, request.getUserId(), request.getName(), request.getPower());
        Device savedDevice = deviceRepository.save(device);
        return convertToDto(savedDevice);
    }

    public List<DeviceDto> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public DeviceDto getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id)); // Excepție simplă
        return convertToDto(device);
    }


    public List<DeviceDto> getDevicesByUserId(Long userId) {
        return deviceRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDevicesByUserId(Long userId) {

        deviceRepository.deleteByUserId(userId);
    }


    private DeviceDto convertToDto(Device device) {
        return new DeviceDto(device.getId(), device.getUserId(), device.getName(), device.getPower());
    }

    public DeviceDto updateDevice(Long id, UpdateDeviceRequest request) {

        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));


        if (request.getName() != null) {
            existingDevice.setName(request.getName());
        }
        if (request.getPower() != null) {
            existingDevice.setPower(request.getPower());
        }

        if (request.getUserId() != null) {
            existingDevice.setUserId(request.getUserId());
        }


        Device updatedDevice = deviceRepository.save(existingDevice);
        return convertToDto(updatedDevice);
    }


    public void deleteDeviceById(Long id) {

        if (!deviceRepository.existsById(id)) {
            throw new RuntimeException("Device not found with id: " + id);
        }
        deviceRepository.deleteById(id);
    }
}