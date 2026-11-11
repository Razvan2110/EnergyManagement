package com.mycompany.userservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "User Management Microservice API",
                version = "1.0",
                description = "Provides CRUD operations for user profiles and data storage."
        ),
        servers = @Server(url = "http://localhost:8082", description = "User Service Server")
)
public class SwaggerConfig { }
