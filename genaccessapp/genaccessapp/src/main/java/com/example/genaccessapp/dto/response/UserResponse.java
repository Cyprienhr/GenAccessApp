package com.example.genaccessapp.dto.response;

import lombok.Data;

import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Long clientId;
    private String clientName;
    private Set<String> roles;
}