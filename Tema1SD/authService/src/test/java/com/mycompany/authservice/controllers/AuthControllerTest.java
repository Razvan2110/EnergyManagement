package com.mycompany.authservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.authservice.model.AuthRequest;
import com.mycompany.authservice.model.UserCredential;
import com.mycompany.authservice.services.AuthService;
import com.mycompany.authservice.services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration-style tests for the AuthController using MockMvc.
 */
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock beans for dependencies
    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationManager authenticationManager;

    private final String TEST_USERNAME = "testUser";
    private final String TEST_PASSWORD = "password";
    private final String TEST_TOKEN = "mocked.jwt.token";
    private final String TEST_ROLE = "ROLE_USER";

    /**
     * Test case for successful user registration.
     */
    @Test
    void addNewUser_success() throws Exception {
        UserCredential requestUser = new UserCredential();
        requestUser.setUsername(TEST_USERNAME);
        requestUser.setPassword(TEST_PASSWORD);

        UserCredential savedUser = new UserCredential();
        // FIX 1: Assuming UserCredential ID is Integer, setting to 1 instead of 1L
        savedUser.setId(1);
        savedUser.setUsername(TEST_USERNAME);

        when(authService.saveUser(any(UserCredential.class))).thenReturn(savedUser);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUser)))
                .andExpect(status().isCreated()) // Expect HTTP 201 Created
                .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                // FIX 1: Asserting against 1 (Integer) instead of 1L (Long)
                .andExpect(jsonPath("$.id").value(1));

        verify(authService, times(1)).saveUser(any(UserCredential.class));
    }

    /**
     * Test case for successful user login and token generation.
     */
    @Test
    void getToken_success() throws Exception {
        // FIX 2: Using setters instead of the constructor to avoid constructor application issues
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(TEST_USERNAME);
        authRequest.setPassword(TEST_PASSWORD);

        // 1. Mock AuthenticationManager to return a valid authenticated object
        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuthentication);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);

        // 2. Mock JwtService to return the token
        when(jwtService.generateToken(mockAuthentication)).thenReturn(TEST_TOKEN);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(result -> assertEquals(TEST_TOKEN, result.getResponse().getContentAsString()));

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(1)).generateToken(mockAuthentication);
    }

    /**
     * Test case for failed user login (bad credentials).
     */
    @Test
    void getToken_failure_badCredentials() throws Exception {
        // FIX 2: Using setters instead of the constructor to avoid constructor application issues
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(TEST_USERNAME);
        authRequest.setPassword(TEST_PASSWORD);

        // Mock AuthenticationManager to throw an exception on failed login
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized()) // Expect HTTP 401 Unauthorized
                // Assert against the specific Romanian error message in the controller
                .andExpect(result -> assertEquals("Nume de utilizator sau parolă incorecte.",
                        result.getResponse().getContentAsString()));

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, never()).generateToken(any());
    }

    /**
     * Test case for successful token validation (plain token).
     */
    @Test
    void validateToken_success_plainToken() throws Exception {
        Map<String, String> requestBody = Map.of("token", TEST_TOKEN);

        // Mock JwtService methods
        when(jwtService.validateToken(TEST_TOKEN)).thenReturn(true);
        when(jwtService.extractUsername(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(jwtService.extractRole(TEST_TOKEN)).thenReturn(TEST_ROLE);

        mockMvc.perform(post("/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.role").value(TEST_ROLE))
                .andExpect(jsonPath("$.message").value("Token JWT valid."));

        verify(jwtService, times(1)).validateToken(TEST_TOKEN);
    }

    /**
     * Test case for successful token validation (Bearer prefix).
     */
    @Test
    void validateToken_success_bearerToken() throws Exception {
        String bearerToken = "Bearer " + TEST_TOKEN;
        Map<String, String> requestBody = Map.of("token", bearerToken);

        // Mock JwtService methods - it should receive the token *without* "Bearer "
        when(jwtService.validateToken(TEST_TOKEN)).thenReturn(true);
        when(jwtService.extractUsername(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(jwtService.extractRole(TEST_TOKEN)).thenReturn(TEST_ROLE);

        mockMvc.perform(post("/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(jsonPath("$.valid").value(true));

        // Verify that JwtService received the cleaned token
        verify(jwtService, times(1)).validateToken(TEST_TOKEN);
    }

    /**
     * Test case for failed token validation (invalid token).
     */
    @Test
    void validateToken_failure_invalidToken() throws Exception {
        Map<String, String> requestBody = Map.of("token", TEST_TOKEN);

        // Mock JwtService to return false for validation
        when(jwtService.validateToken(TEST_TOKEN)).thenReturn(false);

        mockMvc.perform(post("/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized()) // Expect HTTP 401 Unauthorized
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Token JWT invalid sau expirat."));

        verify(jwtService, times(1)).validateToken(TEST_TOKEN);
        // Ensure extraction methods were NOT called since validation failed
        verify(jwtService, never()).extractUsername(any());
        verify(jwtService, never()).extractRole(any());
    }

    /**
     * Test case for missing token in the request body.
     */
    @Test
    void validateToken_failure_missingToken() throws Exception {
        Map<String, String> requestBody = Map.of("token", "");

        mockMvc.perform(post("/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest()) // Expect HTTP 400 Bad Request
                .andExpect(result -> assertEquals("Token-ul lipsește din corp.",
                        result.getResponse().getContentAsString()));
    }
}