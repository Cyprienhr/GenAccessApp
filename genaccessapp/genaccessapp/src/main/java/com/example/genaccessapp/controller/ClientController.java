package com.example.genaccessapp.controller;

import com.example.genaccessapp.dto.request.ClientRequest;
import com.example.genaccessapp.dto.response.ClientResponse;
import com.example.genaccessapp.dto.response.MessageResponse;
import com.example.genaccessapp.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Clients", description = "APIs for managing clients (SUPER_ADMIN only)")
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping
    @Operation(summary = "Get all clients", description = "Retrieve a list of all clients. Requires SUPER_ADMIN role.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        List<ClientResponse> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID", description = "Retrieve a client by its unique ID. Requires SUPER_ADMIN role.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable Long id) {
        ClientResponse client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @PostMapping
    @Operation(summary = "Create a new client", description = "Create a new client. Requires SUPER_ADMIN role.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createClient(@Valid @RequestBody ClientRequest clientRequest) {
        MessageResponse response = clientService.createClient(clientRequest);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a client", description = "Update an existing client by its ID. Requires SUPER_ADMIN role.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequest clientRequest) {
        MessageResponse response = clientService.updateClient(id, clientRequest);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a client", description = "Delete a client by its ID. Requires SUPER_ADMIN role.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        MessageResponse response = clientService.deleteClient(id);
        
        if (response.getMessage().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}