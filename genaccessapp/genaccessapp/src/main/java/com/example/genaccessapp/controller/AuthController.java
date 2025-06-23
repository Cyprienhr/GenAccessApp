package com.example.genaccessapp.controller;

import com.example.genaccessapp.dto.request.LoginRequest;
import com.example.genaccessapp.dto.request.SignupRequest;
import com.example.genaccessapp.dto.response.JwtResponse;
import com.example.genaccessapp.dto.response.MessageResponse;
import com.example.genaccessapp.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticate a user and return a JWT token.")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register a new user. Requires admin privileges.")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        MessageResponse response = authService.registerUser(signUpRequest);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
   
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        MessageResponse response = authService.logoutUser(jwt);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}