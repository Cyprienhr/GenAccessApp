package com.example.genaccessapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class RoleRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    private Set<Long> permissionIds;
}