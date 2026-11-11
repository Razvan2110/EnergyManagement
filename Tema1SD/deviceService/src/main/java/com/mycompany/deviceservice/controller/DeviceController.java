package com.mycompany.deviceservice.controller;

import com.mycompany.deviceservice.dto.DeviceDto;
import com.mycompany.deviceservice.dto.CreateDeviceRequest;
import com.mycompany.deviceservice.dto.UpdateDeviceRequest;
import com.mycompany.deviceservice.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Device Management",
        description = "Endpoints for managing smart energy devices and their associations with users."
)

@RestController
@RequestMapping("/api/v1/devices")
@CrossOrigin(origins = "http://localhost:5173")
public class DeviceController {

    private final DeviceService deviceService;

    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Operation(
            summary = "Create a new device",
            description = "Creates a new smart energy device. Accessible by ADMIN only."
    )
    @ApiResponse(responseCode = "201", description = "Device created successfully")
    @PostMapping
    public ResponseEntity<DeviceDto> createDevice(@RequestBody CreateDeviceRequest request) {
        DeviceDto newDevice = deviceService.createDevice(request);
        return new ResponseEntity<>(newDevice, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Retrieve all devices",
            description = "Returns a list of all devices stored in the database. Accessible by both ADMIN and USER roles."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Devices retrieved successfully"
    )
    @GetMapping
    public ResponseEntity<List<DeviceDto>> getAllDevices() {
        List<DeviceDto> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }

    @Operation(
            summary = "Get device by ID",
            description = "Fetches a specific device based on its ID."
    )
    @ApiResponse(responseCode = "200", description = "Device retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Device not found")
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDto> getDeviceById(@PathVariable Long id) {
        DeviceDto device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }

    @Operation(
            summary = "Get all devices for a specific user",
            description = "Retrieves all devices that belong to a specific user based on their user ID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Devices retrieved successfully for the specified user"
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getDevicesByUserId(@PathVariable Long userId) {

        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        

        Number tokenUserId = (Number) authentication.getDetails();
        
        if (!isAdmin && (tokenUserId == null || tokenUserId.longValue() != userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DeviceDto> devices = deviceService.getDevicesByUserId(userId);
        return ResponseEntity.ok(devices);
    }

    @Operation(
            summary = "Update a device",
            description = "Updates an existing device’s details such as name, power, or associated user."
    )
    @ApiResponse(responseCode = "200", description = "Device updated successfully")
    @ApiResponse(responseCode = "404", description = "Device not found")
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDto> updateDevice(
            @PathVariable Long id,
            @RequestBody UpdateDeviceRequest request) {

        DeviceDto updatedDevice = deviceService.updateDevice(id, request);

        if (updatedDevice != null) {
            return ResponseEntity.ok(updatedDevice);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @Operation(
            summary = "Delete a device by ID",
            description = "Deletes a device from the database based on its ID. Accessible only by ADMIN users."
    )
    @ApiResponse(responseCode = "204", description = "Device deleted successfully")
    @ApiResponse(responseCode = "404", description = "Device not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeviceById(@PathVariable Long id) {

        deviceService.deleteDeviceById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete all devices for a specific user",
            description = "Removes all devices that belong to a specific user. Typically used when a user is deleted."
    )
    @ApiResponse(responseCode = "204", description = "Devices deleted successfully for the given user")
    @DeleteMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    public void deleteDevicesByUserId(@PathVariable Long userId) {

        deviceService.deleteDevicesByUserId(userId);
    }
}