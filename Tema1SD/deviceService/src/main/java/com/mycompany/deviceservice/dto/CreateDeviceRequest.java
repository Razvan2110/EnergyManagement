package com.mycompany.deviceservice.dto;

import lombok.Data;

@Data
public class CreateDeviceRequest {

    private Long userId;
    private String name;
    private int power;
}