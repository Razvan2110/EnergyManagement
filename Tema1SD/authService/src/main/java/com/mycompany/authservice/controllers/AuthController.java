package com.mycompany.authservice.controllers;


import com.mycompany.authservice.model.AuthRequest;
import com.mycompany.authservice.model.UserCredential;
import com.mycompany.authservice.services.AuthService;
import com.mycompany.authservice.services.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthService authService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user credential record.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })

    @PostMapping("/register")
    public ResponseEntity<UserCredential> addNewUser(@RequestBody UserCredential userCredential) {

        UserCredential savedUser = authService.saveUser(userCredential);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token if successful.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    @PostMapping("/login")
    public ResponseEntity<?> getToken(@RequestBody AuthRequest authRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        if (authentication.isAuthenticated()) {

            String token = jwtService.generateToken(authentication);


            return ResponseEntity.ok(token);
        } else {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nume de utilizator sau parolă incorecte.");
        }
    }

    @Operation(summary = "Validate JWT token", description = "Checks if the provided JWT token is valid and returns user info.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "401", description = "Token invalid or expired")
    })
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> tokenMap) {
        String token = tokenMap.get("token");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token-ul lipsește din corp.");
        }


        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (jwtService.validateToken(token)) {

            String username = jwtService.extractUsername(token);

            String role = jwtService.extractRole(token);


            Integer userId = jwtService.extractUserId(token);

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", username,
                    "role", role,
                    "id", userId,
                    "message", "Token JWT valid."
            ));
        } else {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "valid", false,
                    "message", "Token JWT invalid sau expirat."
            ));
        }
    }

}