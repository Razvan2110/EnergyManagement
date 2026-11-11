package com.mycompany.authservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Authentication Microservice API",
                version = "1.0",
                description = "Handles registration, login, JWT validation, and management of user credentials."
        ),
        servers = @Server(url = "http://localhost:8081", description = "Auth Service Server")
)
public class SwaggerConfig { }
