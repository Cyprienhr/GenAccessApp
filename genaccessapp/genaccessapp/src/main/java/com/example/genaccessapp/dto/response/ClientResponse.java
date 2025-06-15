package com.example.genaccessapp.dto.response;

import lombok.Data;

@Data
public class ClientResponse {
    private Long id;
    private String name;
    private String description;
    private int userCount;
    private int roleCount;
}