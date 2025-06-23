package com.example.genaccessapp.service;

import com.example.genaccessapp.dto.request.LoginRequest;
import com.example.genaccessapp.dto.request.SignupRequest;
import com.example.genaccessapp.dto.response.JwtResponse;
import com.example.genaccessapp.dto.response.MessageResponse;
import com.example.genaccessapp.entity.Client;
import com.example.genaccessapp.entity.Role;
import com.example.genaccessapp.entity.User;
import com.example.genaccessapp.repository.ClientRepository;
import com.example.genaccessapp.repository.RoleRepository;
import com.example.genaccessapp.repository.UserRepository;
import com.example.genaccessapp.security.jwt.JwtUtils;
import com.example.genaccessapp.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Set<String> roles = userDetails.getAuthorities().stream()
                .filter(authority -> authority.getAuthority().startsWith("ROLE_"))
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        Set<String> permissions = userDetails.getAuthorities().stream()
                .filter(authority -> !authority.getAuthority().startsWith("ROLE_"))
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getClientId(),
                roles,
                permissions);
    }

    public MessageResponse registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new MessageResponse("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new MessageResponse("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User(
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        // Set client if provided
        if (signUpRequest.getClientId() != null) {
            Client client = clientRepository.findById(signUpRequest.getClientId())
                    .orElseThrow(() -> new RuntimeException("Error: Client not found."));
            user.setClient(client);
        }

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                Role userRole = roleRepository.findByName(role)
                        .orElseThrow(() -> new RuntimeException("Error: Role " + role + " is not found."));
                roles.add(userRole);
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return new MessageResponse("User registered successfully!");
    }
    
    
    public MessageResponse logoutUser(String token) {
        try {
            // Remove the "Bearer " prefix if present
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // Invalidate the token
            jwtUtils.invalidateToken(token);
            
            // Clear the security context
            SecurityContextHolder.clearContext();
            
            return new MessageResponse("Logged out successfully!");
        } catch (Exception e) {
            return new MessageResponse("Error: Failed to logout. " + e.getMessage());
        }
    }
}