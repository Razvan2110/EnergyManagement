package com.mycompany.deviceservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Device Management Microservice API",
                version = "1.0",
                description = "Manages smart energy devices, including CRUD operations and user-device mapping."
        ),
        servers = @Server(url = "http://localhost:8083", description = "Device Service Server")
)
public class SwaggerConfig { }
