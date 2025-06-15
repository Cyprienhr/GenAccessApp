package com.example.genaccessapp.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class RoleResponse {
    private Long id;
    private String name;
    private Long clientId;
    private String clientName;
    private List<String> permissions;
}