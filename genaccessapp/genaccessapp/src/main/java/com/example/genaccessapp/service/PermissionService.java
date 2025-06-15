package com.example.genaccessapp.service;

import com.example.genaccessapp.dto.request.PermissionRequest;
import com.example.genaccessapp.dto.response.MessageResponse;
import com.example.genaccessapp.dto.response.PermissionResponse;
import com.example.genaccessapp.entity.Permission;
import com.example.genaccessapp.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());
    }

    public PermissionResponse getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Permission not found."));
        return mapToPermissionResponse(permission);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('CLIENT_ADMIN')")
    public MessageResponse createPermission(PermissionRequest permissionRequest) {
        if (permissionRepository.existsByName(permissionRequest.getName())) {
            return new MessageResponse("Error: Permission name is already taken!");
        }

        Permission permission = new Permission(
                permissionRequest.getName(),
                permissionRequest.getDescription());

        permissionRepository.save(permission);
        return new MessageResponse("Permission created successfully!");
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('CLIENT_ADMIN')")
    public MessageResponse updatePermission(Long id, PermissionRequest permissionRequest) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Permission not found."));

        // Check if name is taken by another permission
        if (!permission.getName().equals(permissionRequest.getName()) &&
                permissionRepository.existsByName(permissionRequest.getName())) {
            return new MessageResponse("Error: Permission name is already taken!");
        }

        permission.setName(permissionRequest.getName());
        permission.setDescription(permissionRequest.getDescription());

        permissionRepository.save(permission);
        return new MessageResponse("Permission updated successfully!");
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('CLIENT_ADMIN')")
    public MessageResponse deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Permission not found."));

        permissionRepository.delete(permission);
        return new MessageResponse("Permission deleted successfully!");
    }

    private PermissionResponse mapToPermissionResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setName(permission.getName());
        response.setDescription(permission.getDescription());
        return response;
    }
}