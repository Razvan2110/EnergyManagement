package com.mycompany.deviceservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDto {

    private Long id;
    private Long userId;
    private String name;
    private int power;
}