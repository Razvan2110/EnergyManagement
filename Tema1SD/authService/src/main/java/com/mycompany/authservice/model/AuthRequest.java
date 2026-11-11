package com.mycompany.authservice.model;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}