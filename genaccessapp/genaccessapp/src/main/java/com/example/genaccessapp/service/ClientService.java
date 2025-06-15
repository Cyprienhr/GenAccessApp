package com.example.genaccessapp.service;

import com.example.genaccessapp.dto.request.ClientRequest;
import com.example.genaccessapp.dto.response.ClientResponse;
import com.example.genaccessapp.dto.response.MessageResponse;
import com.example.genaccessapp.entity.Client;
import com.example.genaccessapp.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::mapToClientResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Client not found."));
        return mapToClientResponse(client);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public MessageResponse createClient(ClientRequest clientRequest) {
        if (clientRepository.existsByName(clientRequest.getName())) {
            return new MessageResponse("Error: Client name is already taken!");
        }

        Client client = new Client(
                clientRequest.getName(),
                clientRequest.getDescription());

        clientRepository.save(client);
        return new MessageResponse("Client created successfully!");
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public MessageResponse updateClient(Long id, ClientRequest clientRequest) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Client not found."));

        // Check if name is taken by another client
        if (!client.getName().equals(clientRequest.getName()) &&
                clientRepository.existsByName(clientRequest.getName())) {
            return new MessageResponse("Error: Client name is already taken!");
        }

        client.setName(clientRequest.getName());
        client.setDescription(clientRequest.getDescription());

        clientRepository.save(client);
        return new MessageResponse("Client updated successfully!");
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public MessageResponse deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Client not found."));

        // Check if client has users or roles
        if (!client.getUsers().isEmpty()) {
            return new MessageResponse("Error: Cannot delete client with users. Delete users first.");
        }

        if (!client.getRoles().isEmpty()) {
            return new MessageResponse("Error: Cannot delete client with roles. Delete roles first.");
        }

        clientRepository.delete(client);
        return new MessageResponse("Client deleted successfully!");
    }

    private ClientResponse mapToClientResponse(Client client) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setName(client.getName());
        response.setDescription(client.getDescription());
        response.setUserCount(client.getUsers().size());
        response.setRoleCount(client.getRoles().size());
        return response;
    }
}