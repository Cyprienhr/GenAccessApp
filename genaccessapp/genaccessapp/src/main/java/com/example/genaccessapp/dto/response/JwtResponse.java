package com.example.genaccessapp.dto.response;

import lombok.Data;

import java.util.Set;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Long clientId;
    private Set<String> roles;
    private Set<String> permissions;

    public JwtResponse(String token, Long id, String firstName, String lastName, String username, String email, Long clientId, Set<String> roles, Set<String> permissions) {
        this.token = token;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.clientId = clientId;
        this.roles = roles;
        this.permissions = permissions;
    }
    
    public JwtResponse(String token, String type, Long id, String firstName, String lastName, String username, String email, Long clientId, Set<String> roles, Set<String> permissions) {
        this.token = token;
        this.type = type;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.clientId = clientId;
        this.roles = roles;
        this.permissions = permissions;
    }
}