package com.mycompany.userservice.dto;

import com.mycompany.userservice.model.Role;
import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
}