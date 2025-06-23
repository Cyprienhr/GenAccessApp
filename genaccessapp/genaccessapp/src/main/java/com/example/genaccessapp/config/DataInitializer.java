package com.example.genaccessapp.config;

import com.example.genaccessapp.entity.Client;
import com.example.genaccessapp.entity.Permission;
import com.example.genaccessapp.entity.Role;
import com.example.genaccessapp.entity.User;
import com.example.genaccessapp.repository.ClientRepository;
import com.example.genaccessapp.repository.PermissionRepository;
import com.example.genaccessapp.repository.RoleRepository;
import com.example.genaccessapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeDefaultData();
    }

    private void initializeDefaultData() {
        Client systemClient = createDefaultClient();
        List<Permission> permissions = createDefaultPermissions();
        Role superAdminRole = createDefaultRole("SUPER_ADMIN", systemClient);
        Role clientAdminRole = createDefaultRole("CLIENT_ADMIN", systemClient);
        Role userRole = createDefaultRole("USER", systemClient);

        // Assign permissions to roles
        assignPermissionsToRoles(superAdminRole, clientAdminRole, userRole, permissions);

        // Create default super admin user if it doesn't exist
        createDefaultSuperAdminUser(systemClient, superAdminRole);
    }

    private Client createDefaultClient() {
        Optional<Client> existingClient = clientRepository.findByName("System");
        if (existingClient.isPresent()) {
            return existingClient.get();
        }

        Client systemClient = new Client("System", "System default client");
        return clientRepository.save(systemClient);
    }

    private List<Permission> createDefaultPermissions() {
        List<Permission> defaultPermissions = Arrays.asList(
                new Permission("user_read", "Permission to read user data"),
                new Permission("user_write", "Permission to create/update user data"),
                new Permission("user_delete", "Permission to delete user data"),
                new Permission("role_read", "Permission to read role data"),
                new Permission("role_write", "Permission to create/update role data"),
                new Permission("role_delete", "Permission to delete role data"),
                new Permission("client_read", "Permission to read client data"),
                new Permission("client_write", "Permission to create/update client data"),
                new Permission("client_delete", "Permission to delete client data")
        );

        for (Permission permission : defaultPermissions) {
            Optional<Permission> existingPermission = permissionRepository.findByName(permission.getName());
            if (!existingPermission.isPresent()) {
                permissionRepository.save(permission);
            }
        }

        return permissionRepository.findAll();
    }

    private Role createDefaultRole(String roleName, Client client) {
        Optional<Role> existingRole = roleRepository.findByNameAndClientId(roleName, client.getId());
        if (existingRole.isPresent()) {
            return existingRole.get();
        }

        Role role = new Role(roleName, client);
        return roleRepository.save(role);
    }

    private void assignPermissionsToRoles(Role superAdminRole, Role clientAdminRole, Role userRole, List<Permission> permissions) {
        // Assign all permissions to SUPER_ADMIN role
        superAdminRole.setPermissions(new HashSet<>(permissions));
        roleRepository.save(superAdminRole);

        // Assign client-specific permissions to CLIENT_ADMIN role
        HashSet<Permission> clientAdminPermissions = new HashSet<>();
        for (Permission permission : permissions) {
            if (permission.getName().equals("user_read") || 
                permission.getName().equals("user_write") || 
                permission.getName().equals("user_delete") || 
                permission.getName().equals("role_read") || 
                permission.getName().equals("role_write") || 
                permission.getName().equals("role_delete")) {
                clientAdminPermissions.add(permission);
            }
        }
        clientAdminRole.setPermissions(clientAdminPermissions);
        roleRepository.save(clientAdminRole);

        // Assign basic permissions to USER role
        HashSet<Permission> userPermissions = new HashSet<>();
        for (Permission permission : permissions) {
            if (permission.getName().equals("user_read")) {
                userPermissions.add(permission);
            }
        }
        userRole.setPermissions(userPermissions);
        roleRepository.save(userRole);
    }

    private void createDefaultSuperAdminUser(Client systemClient, Role superAdminRole) {
        if (userRepository.findByUsername("Cyprienhr").isPresent()) {
            return;
        }

        User superAdmin = new User(
                "Cyprien",
                "Rwendere",
                "Cyprienhr",
                "rwendere@gmail.com",
                passwordEncoder.encode("Rwendere@2001")
        );
        superAdmin.setClient(systemClient);
        superAdmin.setRoles(new HashSet<>(Arrays.asList(superAdminRole)));
        userRepository.save(superAdmin);
    }
}
