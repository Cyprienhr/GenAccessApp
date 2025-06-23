package com.example.genaccessapp.service;

import com.example.genaccessapp.dto.request.RoleRequest;
import com.example.genaccessapp.dto.response.MessageResponse;
import com.example.genaccessapp.dto.response.RoleResponse;
import com.example.genaccessapp.entity.Client;
import com.example.genaccessapp.entity.Permission;
import com.example.genaccessapp.entity.Role;
import com.example.genaccessapp.repository.ClientRepository;
import com.example.genaccessapp.repository.PermissionRepository;
import com.example.genaccessapp.repository.RoleRepository;
import com.example.genaccessapp.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private ClientRepository clientRepository;

    public List<RoleResponse> getAllRoles() {
        UserDetailsImpl currentUser = getCurrentUser();
        List<Role> roles;

        if (isSuperAdmin()) {
            // Super admin can see all roles
            roles = roleRepository.findAll();
        } else {
            // Client admin can only see roles for their client
            roles = roleRepository.findByClientId(currentUser.getClientId());
        }

        return roles.stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    public RoleResponse getRoleById(Long id) {
        UserDetailsImpl currentUser = getCurrentUser();
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));

        // Check if user has access to this role
        if (!isSuperAdmin() && 
            (role.getClient() == null || !role.getClient().getId().equals(currentUser.getClientId()))) {
            throw new RuntimeException("Error: You don't have permission to access this role.");
        }

        return mapToRoleResponse(role);
    }

    public MessageResponse createRole(RoleRequest roleRequest) {
        UserDetailsImpl currentUser = getCurrentUser();

        // Check if role name already exists for the client
        if (isSuperAdmin()) {
            if (roleRepository.existsByName(roleRequest.getName())) {
                return new MessageResponse("Error: Role name is already taken!");
            }
        } else {
            if (roleRepository.existsByNameAndClientId(roleRequest.getName(), currentUser.getClientId())) {
                return new MessageResponse("Error: Role name is already taken for this client!");
            }
        }

        // Create new role
        Role role = new Role(roleRequest.getName());

        // Set client
        if (!isSuperAdmin()) {
            // Client admin can only create roles for their client
            Client client = clientRepository.findById(currentUser.getClientId())
                    .orElseThrow(() -> new RuntimeException("Error: Client not found."));
            role.setClient(client);
        }

        // Set permissions
        if (roleRequest.getPermissionIds() != null && !roleRequest.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>();
            roleRequest.getPermissionIds().forEach(permissionId -> {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> new RuntimeException("Error: Permission not found with id " + permissionId));
                permissions.add(permission);
            });
            role.setPermissions(permissions);
        }

        roleRepository.save(role);
        return new MessageResponse("Role created successfully!");
    }

    public MessageResponse updateRole(Long id, RoleRequest roleRequest) {
        UserDetailsImpl currentUser = getCurrentUser();
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));

        // Check if user has access to update this role
        if (!isSuperAdmin() && 
            (role.getClient() == null || !role.getClient().getId().equals(currentUser.getClientId()))) {
            return new MessageResponse("Error: You don't have permission to update this role.");
        }

        // Check if role name is taken by another role
        if (!role.getName().equals(roleRequest.getName())) {
            if (isSuperAdmin()) {
                if (roleRepository.existsByName(roleRequest.getName())) {
                    return new MessageResponse("Error: Role name is already taken!");
                }
            } else {
                if (roleRepository.existsByNameAndClientId(roleRequest.getName(), currentUser.getClientId())) {
                    return new MessageResponse("Error: Role name is already taken for this client!");
                }
            }
        }

        // Update role details
        role.setName(roleRequest.getName());

        // Update permissions if provided
        if (roleRequest.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>();
            roleRequest.getPermissionIds().forEach(permissionId -> {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> new RuntimeException("Error: Permission not found with id " + permissionId));
                permissions.add(permission);
            });
            role.setPermissions(permissions);
        }

        roleRepository.save(role);
        return new MessageResponse("Role updated successfully!");
    }

    public MessageResponse deleteRole(Long id) {
        UserDetailsImpl currentUser = getCurrentUser();
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));

        // Check if user has access to delete this role
        if (!isSuperAdmin() && 
            (role.getClient() == null || !role.getClient().getId().equals(currentUser.getClientId()))) {
            return new MessageResponse("Error: You don't have permission to delete this role.");
        }

        roleRepository.delete(role);
        return new MessageResponse("Role deleted successfully!");
    }

    private RoleResponse mapToRoleResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        
        if (role.getClient() != null) {
            response.setClientId(role.getClient().getId());
            response.setClientName(role.getClient().getName());
        }
        
        response.setPermissions(role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toList()));
        
        return response;
    }

    private UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetailsImpl) authentication.getPrincipal();
    }

    private boolean isSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}