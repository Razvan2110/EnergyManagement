package com.mycompany.deviceservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeviceRequest {
    private Long userId;
    private String name;
    private Integer power;
}