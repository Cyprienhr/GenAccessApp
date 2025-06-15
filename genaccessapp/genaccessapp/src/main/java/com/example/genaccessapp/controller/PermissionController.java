package com.example.genaccessapp.controller;

import com.example.genaccessapp.dto.request.PermissionRequest;
import com.example.genaccessapp.dto.response.MessageResponse;
import com.example.genaccessapp.dto.response.PermissionResponse;
import com.example.genaccessapp.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Permissions", description = "APIs for managing permissions")
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping
    @Operation(summary = "Get all permissions", description = "Retrieve a list of all permissions.")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get permission by ID", description = "Retrieve a permission by its unique ID.")
    public ResponseEntity<PermissionResponse> getPermissionById(@PathVariable Long id) {
        PermissionResponse permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(permission);
    }

    @PostMapping
    @Operation(summary = "Create a new permission", description = "Create a new permission. Requires SUPER_ADMIN or CLIENT_ADMIN role.")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('CLIENT_ADMIN')")
    public ResponseEntity<?> createPermission(@Valid @RequestBody PermissionRequest permissionRequest) {
        MessageResponse response = permissionService.createPermission(permissionRequest);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a permission", description = "Update an existing permission by its ID. Requires SUPER_ADMIN or CLIENT_ADMIN role.")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('CLIENT_ADMIN')")
    public ResponseEntity<?> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionRequest permissionRequest) {
        MessageResponse response = permissionService.updatePermission(id, permissionRequest);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a permission", description = "Delete a permission by its ID. Requires SUPER_ADMIN or CLIENT_ADMIN role.")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('CLIENT_ADMIN')")
    public ResponseEntity<?> deletePermission(@PathVariable Long id) {
        MessageResponse response = permissionService.deletePermission(id);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}