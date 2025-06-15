package com.example.genaccessapp.controller;

import com.example.genaccessapp.dto.request.SignupRequest;
import com.example.genaccessapp.dto.response.MessageResponse;
import com.example.genaccessapp.dto.response.UserResponse;
import com.example.genaccessapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@Tag(name = "Users", description = "APIs for managing users")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all users. Requires CLIENT_ADMIN or SUPER_ADMIN role.")
    @PreAuthorize("hasRole('CLIENT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user by their unique ID. Requires CLIENT_ADMIN or SUPER_ADMIN role.")
    @PreAuthorize("hasRole('CLIENT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user. Requires CLIENT_ADMIN or SUPER_ADMIN role.")
    @PreAuthorize("hasRole('CLIENT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody SignupRequest signupRequest) {
        MessageResponse response = userService.createUser(signupRequest);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user", description = "Update an existing user by their ID. Requires CLIENT_ADMIN or SUPER_ADMIN role.")
    @PreAuthorize("hasRole('CLIENT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody SignupRequest signupRequest) {
        MessageResponse response = userService.updateUser(id, signupRequest);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Delete a user by their ID. Requires CLIENT_ADMIN or SUPER_ADMIN role.")
    @PreAuthorize("hasRole('CLIENT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        MessageResponse response = userService.deleteUser(id);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}