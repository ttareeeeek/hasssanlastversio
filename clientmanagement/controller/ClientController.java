package com.yourcompany.clientmanagement.controller;

import com.yourcompany.clientmanagement.dao.ClientDAO;
import com.yourcompany.clientmanagement.model.Client;

import java.util.List;

public class ClientController {
    private ClientDAO clientDAO;

    public ClientController() {
        clientDAO = new ClientDAO();
    }

    // 🔄 1. Fetch all clients
    public List<Client> fetchAllClients() {
        return clientDAO.getAllClients();
    }

    // ➕ 2. Add a client
    public int addClient(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }
        if (client.getNom() == null || client.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Client name is required");
        }
        if (client.getActivite() == null || client.getActivite().trim().isEmpty()) {
            throw new IllegalArgumentException("Client activity is required");
        }
        return clientDAO.insertClient(client);
    }

    // ✏️ 3. Update a client
    public boolean updateClient(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }
        if (client.getId() <= 0) {
            throw new IllegalArgumentException("Valid client ID is required for update");
        }
        if (client.getNom() == null || client.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Client name is required");
        }
        if (client.getActivite() == null || client.getActivite().trim().isEmpty()) {
            throw new IllegalArgumentException("Client activity is required");
        }
        return clientDAO.updateClient(client);
    }

    // ❌ 4. Delete a client by ID
    public boolean deleteClient(int clientId) {
        if (clientId <= 0) {
            throw new IllegalArgumentException("Valid client ID is required");
        }
        return clientDAO.deleteClientById(clientId);
    }

    // 🔎 5. Optional: Search by name or other field
    public List<Client> searchClients(String keyword) {
        if (keyword == null) {
            keyword = "";
        }
        return clientDAO.searchClients(keyword);
    }
    
    public Client getClientById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Valid client ID is required");
        }
        return clientDAO.getClientById(id);
    }
}
