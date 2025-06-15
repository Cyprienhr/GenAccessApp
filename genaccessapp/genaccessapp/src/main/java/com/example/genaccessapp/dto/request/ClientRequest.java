package com.example.genaccessapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientRequest {
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Size(max = 200)
    private String description;
}