package com.example.genaccessapp.controller;

import com.example.genaccessapp.dto.request.RoleRequest;
import com.example.genaccessapp.dto.response.MessageResponse;
import com.example.genaccessapp.dto.response.RoleResponse;
import com.example.genaccessapp.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@Tag(name = "Roles", description = "APIs for managing roles and assigning permissions")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    @Operation(summary = "Get all roles", description = "Retrieve a list of all roles. Requires CLIENT_ADMIN or SUPER_ADMIN role.")
    @PreAuthorize("hasRole('CLIENT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", description = "Retrieve a role by its unique ID. Requires CLIENT_ADMIN or SUPER_ADMIN role.")
    @PreAuthorize("hasRole('CLIENT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    @PostMapping
    @Operation(summary = "Create a new role", description = "Create a new role and assign permissions. Requires CLIENT_ADMIN or SUPER_ADMIN role.")
    @PreAuthorize("hasRole('CLIENT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleRequest roleRequest) {
        MessageResponse response = roleService.createRole(roleRequest);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a role", description = "Update an existing role by its ID. Requires CLIENT_ADMIN or SUPER_ADMIN role.")
    @PreAuthorize("hasRole('CLIENT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest roleRequest) {
        MessageResponse response = roleService.updateRole(id, roleRequest);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a role", description = "Delete a role by its ID. Requires CLIENT_ADMIN or SUPER_ADMIN role.")
    @PreAuthorize("hasRole('CLIENT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        MessageResponse response = roleService.deleteRole(id);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}