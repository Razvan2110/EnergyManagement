package com.mycompany.deviceservice.service;

import com.mycompany.deviceservice.dto.CreateDeviceRequest;
import com.mycompany.deviceservice.dto.DeviceDto;
import com.mycompany.deviceservice.dto.UpdateDeviceRequest;
import com.mycompany.deviceservice.model.Device;
import com.mycompany.deviceservice.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviceServiceTest {

    @Mock // Simulează DeviceRepository
    private DeviceRepository deviceRepository;

    @InjectMocks // Injectează mock-urile (deviceRepository) în clasa DeviceService
    private DeviceService deviceService;

    // Obiecte de test comune
    private Device testDevice;
    private DeviceDto testDeviceDto;
    private CreateDeviceRequest testCreateRequest;

    // În metoda setUp() din DeviceServiceTest:
    // Modificare aplicată în DeviceServiceTest:

    @BeforeEach
    void setUp() {
        testDevice = new Device(1L, 100L, "Smart Light", 10);
        testDeviceDto = new DeviceDto(1L, 100L, "Smart Light", 10);

        // Asigură că constructorul fără argumente și seterele funcționează
        testCreateRequest = new CreateDeviceRequest();
        testCreateRequest.setUserId(100L);
        testCreateRequest.setName("Smart Light");
        testCreateRequest.setPower(10);
    }
// ... restul clasei de test

    // --- Teste CRUD de Bază ---

    @Test
    void createDevice_ShouldSaveAndReturnDto() {
        // Simulează comportamentul metodei save() din Repository
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

        DeviceDto result = deviceService.createDevice(testCreateRequest);

        // Verifică
        assertNotNull(result);
        assertEquals(testDeviceDto.getName(), result.getName());
        assertEquals(testDeviceDto.getUserId(), result.getUserId());
        // Verifică dacă metoda save a fost apelată o singură dată
        verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    void getDeviceById_ShouldReturnDeviceDto_WhenFound() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));

        DeviceDto result = deviceService.getDeviceById(1L);

        assertNotNull(result);
        assertEquals(testDeviceDto.getId(), result.getId());
    }

    @Test
    void getDeviceById_ShouldThrowException_WhenNotFound() {
        when(deviceRepository.findById(2L)).thenReturn(Optional.empty());

        // Verifică dacă este aruncată excepția așteptată
        assertThrows(RuntimeException.class, () -> deviceService.getDeviceById(2L));
    }

    @Test
    void getAllDevices_ShouldReturnAllDeviceDtos() {
        List<Device> deviceList = Arrays.asList(testDevice, new Device(2L, 100L, "Smart Plug", 5));
        when(deviceRepository.findAll()).thenReturn(deviceList);

        List<DeviceDto> result = deviceService.getAllDevices();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Smart Plug", result.get(1).getName());
    }

    @Test
    void updateDevice_ShouldUpdateAndReturnDto_WhenFound() {
        UpdateDeviceRequest updateRequest = new UpdateDeviceRequest(null, "New Name", 20);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        // Simulează salvarea după update
        Device updatedDevice = new Device(1L, 100L, "New Name", 20);
        when(deviceRepository.save(any(Device.class))).thenReturn(updatedDevice);

        DeviceDto result = deviceService.updateDevice(1L, updateRequest);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals(20, result.getPower());
        // userId rămâne același (100L) deoarece nu a fost trimis în request
        assertEquals(100L, result.getUserId());
    }

    @Test
    void updateDevice_ShouldThrowException_WhenNotFound() {
        UpdateDeviceRequest updateRequest = new UpdateDeviceRequest(null, "New Name", 20);
        when(deviceRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> deviceService.updateDevice(2L, updateRequest));
    }

    @Test
    void deleteDeviceById_ShouldDelete_WhenExists() {
        when(deviceRepository.existsById(1L)).thenReturn(true);
        doNothing().when(deviceRepository).deleteById(1L);

        deviceService.deleteDeviceById(1L);

        verify(deviceRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteDeviceById_ShouldThrowException_WhenNotExists() {
        when(deviceRepository.existsById(2L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> deviceService.deleteDeviceById(2L));
        verify(deviceRepository, never()).deleteById(anyLong());
    }

    // --- Teste Logica Specifică ---

    @Test
    void getDevicesByUserId_ShouldReturnFilteredDtos() {
        Long userId = 100L;
        List<Device> userDevices = Arrays.asList(testDevice, new Device(2L, userId, "Smart Plug", 5));
        when(deviceRepository.findByUserId(userId)).thenReturn(userDevices);

        List<DeviceDto> result = deviceService.getDevicesByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(d -> d.getUserId().equals(userId)));
        verify(deviceRepository, times(1)).findByUserId(userId);
    }

    @Test
    void deleteDevicesByUserId_ShouldCallRepositoryMethod() {
        Long userId = 100L;
        doNothing().when(deviceRepository).deleteByUserId(userId);

        deviceService.deleteDevicesByUserId(userId);

        // Verifică dacă metoda de ștergere în cascadă a fost apelată
        verify(deviceRepository, times(1)).deleteByUserId(userId);
    }
}