package com.mycompany.deviceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.deviceservice.dto.CreateDeviceRequest;
import com.mycompany.deviceservice.dto.DeviceDto;
import com.mycompany.deviceservice.dto.UpdateDeviceRequest;
import com.mycompany.deviceservice.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
public class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // Păstrat, deși generează avertismentul de depreciere
    private DeviceService deviceService;

    private DeviceDto testDeviceDto;
    private CreateDeviceRequest testCreateRequest;
    private final String BASE_URL = "/api/v1/devices";

    @BeforeEach
    void setUp() {
        testDeviceDto = new DeviceDto(1L, 100L, "Smart Light", 10);

        // REZOLVAT: Inițializare prin setteri pentru a evita eroarea de constructor
        testCreateRequest = new CreateDeviceRequest();
        testCreateRequest.setUserId(100L);
        testCreateRequest.setName("Smart Light");
        testCreateRequest.setPower(10);
    }

    // --- Teste POST ---
    @Test
    void createDevice_ShouldReturnCreatedStatusAndDto() throws Exception {
        when(deviceService.createDevice(any(CreateDeviceRequest.class))).thenReturn(testDeviceDto);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Smart Light")));

        verify(deviceService, times(1)).createDevice(any(CreateDeviceRequest.class));
    }

    // ... (Restul metodelor de test sunt corecte)
    @Test
    void getDeviceById_ShouldReturnOkStatusAndDto_WhenFound() throws Exception {
        when(deviceService.getDeviceById(1L)).thenReturn(testDeviceDto);

        mockMvc.perform(get(BASE_URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userId", is(100)));
    }

    @Test
    void getAllDevices_ShouldReturnOkStatusAndList() throws Exception {
        List<DeviceDto> deviceList = Arrays.asList(testDeviceDto, new DeviceDto(2L, 100L, "Smart Plug", 5));
        when(deviceService.getAllDevices()).thenReturn(deviceList);

        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Smart Light")));
    }

    @Test
    void getDevicesByUserId_ShouldReturnOkStatusAndList() throws Exception {
        Long userId = 100L;
        List<DeviceDto> userDevices = Arrays.asList(testDeviceDto, new DeviceDto(2L, userId, "Smart Plug", 5));
        when(deviceService.getDevicesByUserId(userId)).thenReturn(userDevices);

        mockMvc.perform(get(BASE_URL + "/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].name", is("Smart Plug")));

        verify(deviceService, times(1)).getDevicesByUserId(userId);
    }

    @Test
    void updateDevice_ShouldReturnOkStatusAndUpdatedDto() throws Exception {
        UpdateDeviceRequest updateRequest = new UpdateDeviceRequest(null, "New Name", 20);
        DeviceDto updatedDto = new DeviceDto(1L, 100L, "New Name", 20);

        when(deviceService.updateDevice(eq(1L), any(UpdateDeviceRequest.class))).thenReturn(updatedDto);

        mockMvc.perform(put(BASE_URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Name")))
                .andExpect(jsonPath("$.power", is(20)));

        verify(deviceService, times(1)).updateDevice(eq(1L), any(UpdateDeviceRequest.class));
    }

    @Test
    void deleteDeviceById_ShouldReturnNoContentStatus() throws Exception {
        doNothing().when(deviceService).deleteDeviceById(1L);

        mockMvc.perform(delete(BASE_URL + "/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(deviceService, times(1)).deleteDeviceById(1L);
    }

    @Test
    void deleteDevicesByUserId_ShouldReturnNoContentStatus() throws Exception {
        Long userId = 100L;
        doNothing().when(deviceService).deleteDevicesByUserId(userId);

        mockMvc.perform(delete(BASE_URL + "/user/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(deviceService, times(1)).deleteDevicesByUserId(userId);
    }
}