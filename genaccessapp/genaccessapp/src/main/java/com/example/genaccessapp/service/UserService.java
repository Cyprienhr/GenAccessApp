package com.example.genaccessapp.service;

import com.example.genaccessapp.dto.request.SignupRequest;
import com.example.genaccessapp.dto.response.MessageResponse;
import com.example.genaccessapp.dto.response.UserResponse;
import com.example.genaccessapp.entity.Client;
import com.example.genaccessapp.entity.Role;
import com.example.genaccessapp.entity.User;
import com.example.genaccessapp.repository.ClientRepository;
import com.example.genaccessapp.repository.RoleRepository;
import com.example.genaccessapp.repository.UserRepository;
import com.example.genaccessapp.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder encoder;

    public List<UserResponse> getAllUsers() {
        UserDetailsImpl currentUser = getCurrentUser();
        List<User> users;

        // If super admin, get all users
        if (isSuperAdmin()) {
            users = userRepository.findAll();
        } else {
            // If client admin, get only users from their client
            users = userRepository.findByClientId(currentUser.getClientId());
        }

        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        UserDetailsImpl currentUser = getCurrentUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // Check if user has access to this user
        if (!isSuperAdmin() && !user.getClient().getId().equals(currentUser.getClientId())) {
            throw new RuntimeException("Error: You don't have permission to access this user.");
        }

        return mapToUserResponse(user);
    }

    public MessageResponse createUser(SignupRequest signupRequest) {
        UserDetailsImpl currentUser = getCurrentUser();

        // Check if username or email already exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return new MessageResponse("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return new MessageResponse("Error: Email is already in use!");
        }

        // Create new user
        User user = new User(
                signupRequest.getFirstName(),
                signupRequest.getLastName(),
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword()));

        // Set client
        if (isSuperAdmin()) {
            // Super admin can create users for any client
            if (signupRequest.getClientId() != null) {
                Client client = clientRepository.findById(signupRequest.getClientId())
                        .orElseThrow(() -> new RuntimeException("Error: Client not found."));
                user.setClient(client);
            }
        } else {
            // Client admin can only create users for their client
            Client client = clientRepository.findById(currentUser.getClientId())
                    .orElseThrow(() -> new RuntimeException("Error: Client not found."));
            user.setClient(client);
        }

        // Set roles
        Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                Role userRole;
                if (isSuperAdmin()) {
                    userRole = roleRepository.findByName(role)
                            .orElseThrow(() -> new RuntimeException("Error: Role " + role + " is not found."));
                } else {
                    // Client admin can only assign roles from their client
                    userRole = roleRepository.findByNameAndClientId(role, currentUser.getClientId())
                            .orElseThrow(() -> new RuntimeException("Error: Role " + role + " is not found or not accessible."));
                }
                roles.add(userRole);
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return new MessageResponse("User created successfully!");
    }

    public MessageResponse updateUser(Long id, SignupRequest signupRequest) {
        UserDetailsImpl currentUser = getCurrentUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // Check if user has access to update this user
        if (!isSuperAdmin() && !user.getClient().getId().equals(currentUser.getClientId())) {
            return new MessageResponse("Error: You don't have permission to update this user.");
        }

        // Check if username is taken by another user
        if (!user.getUsername().equals(signupRequest.getUsername()) &&
                userRepository.existsByUsername(signupRequest.getUsername())) {
            return new MessageResponse("Error: Username is already taken!");
        }

        // Check if email is taken by another user
        if (!user.getEmail().equals(signupRequest.getEmail()) &&
                userRepository.existsByEmail(signupRequest.getEmail())) {
            return new MessageResponse("Error: Email is already in use!");
        }

        // Update user details
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());

        // Update password if provided
        if (signupRequest.getPassword() != null && !signupRequest.getPassword().isEmpty()) {
            user.setPassword(encoder.encode(signupRequest.getPassword()));
        }

        // Update client if super admin and client ID is provided
        if (isSuperAdmin() && signupRequest.getClientId() != null) {
            Client client = clientRepository.findById(signupRequest.getClientId())
                    .orElseThrow(() -> new RuntimeException("Error: Client not found."));
            user.setClient(client);
        }

        // Update roles if provided
        if (signupRequest.getRoles() != null && !signupRequest.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            signupRequest.getRoles().forEach(role -> {
                Role userRole;
                if (isSuperAdmin()) {
                    userRole = roleRepository.findByName(role)
                            .orElseThrow(() -> new RuntimeException("Error: Role " + role + " is not found."));
                } else {
                    // Client admin can only assign roles from their client
                    userRole = roleRepository.findByNameAndClientId(role, currentUser.getClientId())
                            .orElseThrow(() -> new RuntimeException("Error: Role " + role + " is not found or not accessible."));
                }
                roles.add(userRole);
            });
            user.setRoles(roles);
        }

        userRepository.save(user);

        return new MessageResponse("User updated successfully!");
    }

    public MessageResponse deleteUser(Long id) {
        UserDetailsImpl currentUser = getCurrentUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // Check if user has access to delete this user
        if (!isSuperAdmin() && !user.getClient().getId().equals(currentUser.getClientId())) {
            return new MessageResponse("Error: You don't have permission to delete this user.");
        }

        userRepository.delete(user);
        return new MessageResponse("User deleted successfully!");
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        
        if (user.getClient() != null) {
            response.setClientId(user.getClient().getId());
            response.setClientName(user.getClient().getName());
        }
        
        response.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        
        return response;
    }

    private UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetailsImpl) authentication.getPrincipal();
    }

    private boolean isSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("SUPER_ADMIN"));
    }
}