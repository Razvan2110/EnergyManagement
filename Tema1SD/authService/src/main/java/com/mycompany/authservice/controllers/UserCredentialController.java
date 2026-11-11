package com.mycompany.authservice.controllers;


import com.mycompany.authservice.model.UserCredential;
import com.mycompany.authservice.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "User Credentials Management", description = "Endpoints for managing authentication credentials")
@RestController
@RequestMapping("/users")
public class UserCredentialController {

    private final AuthService authService;

    public UserCredentialController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Get all credential records")
    @ApiResponse(responseCode = "200", description = "List of all credential users")
    @GetMapping
    public ResponseEntity<List<UserCredential>> getAllUsers() {
        return ResponseEntity.ok(authService.findAllUsers());
    }

    @Operation(summary = "Get credentials by ID")
    @ApiResponse(responseCode = "200", description = "Credential found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    public ResponseEntity<UserCredential> getUserById(@PathVariable Integer id) {
        return authService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new credential entry (admin use)")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @PostMapping
    public ResponseEntity<UserCredential> createUser(@RequestBody UserCredential userCredential) {
        UserCredential savedUser = authService.saveUser(userCredential);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Operation(summary = "Update existing user credentials")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/{id}")
    public ResponseEntity<UserCredential> updateUser(@PathVariable Integer id, @RequestBody UserCredential userDetails) {
        try {
            UserCredential updatedUser = authService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete user credentials by ID")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}