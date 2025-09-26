package com.yourcompany.clientmanagement.controller;

import com.yourcompany.clientmanagement.dao.VersmentDAO;
import com.yourcompany.clientmanagement.dao.ClientDAO;
import com.yourcompany.clientmanagement.model.Versment;
import com.yourcompany.clientmanagement.model.Client;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

public class VersmentController {
    private VersmentDAO versmentDAO;
    private ClientDAO clientDAO;

    public VersmentController() {
        versmentDAO = new VersmentDAO();
        clientDAO = new ClientDAO();
    }

    // 🔄 1. Fetch all versments
    public List<Versment> fetchAllVersments() {
        return versmentDAO.getAllVersments();
    }

    // 🔄 2. Fetch versments by client ID
    public List<Versment> fetchVersmentsByClientId(int clientId) {
        return versmentDAO.getVersmentsByClientId(clientId);
    }

    // ➕ 3. Add a versment
    public int addVersment(Versment versment) {
        if (versment == null) {
            throw new IllegalArgumentException("Versment cannot be null");
        }
        if (versment.getClientId() <= 0) {
            throw new IllegalArgumentException("Valid client ID is required");
        }
        if (versment.getMontant() == null || versment.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valid amount is required");
        }

        // Insert the versment first
        int versmentId = versmentDAO.insertVersment(versment);
        
        if (versmentId > 0) {
            // Update client's remaining balance by reducing the versment amount
            updateClientRemainingBalanceAfterVersment(versment.getClientId(), versment.getMontant(), false);
        }
        
        return versmentId;
    }

    // ✏️ 4. Update a versment
    public boolean updateVersment(Versment versment) {
        if (versment == null) {
            throw new IllegalArgumentException("Versment cannot be null");
        }
        if (versment.getId() <= 0) {
            throw new IllegalArgumentException("Valid versment ID is required for update");
        }
        
        // Get the original versment to calculate the difference
        Versment originalVersment = versmentDAO.getVersmentById(versment.getId());
        if (originalVersment == null) {
            throw new IllegalArgumentException("Original versment not found");
        }
        
        boolean success = versmentDAO.updateVersment(versment);
        
        if (success) {
            // Calculate the difference and update client's montant
            BigDecimal difference = versment.getMontant().subtract(originalVersment.getMontant());
            if (difference.compareTo(BigDecimal.ZERO) != 0) {
                // If difference is positive, we need to reduce more from client's remaining balance
                // If difference is negative, we need to add back to client's remaining balance
                updateClientRemainingBalanceAfterVersment(versment.getClientId(), difference, false);
            }
        }
        
        return success;
    }

    // ❌ 5. Delete a versment by ID
    public boolean deleteVersment(int versmentId) {
        if (versmentId <= 0) {
            throw new IllegalArgumentException("Valid versment ID is required");
        }
        
        // Get the versment before deleting to restore the client's montant
        Versment versment = versmentDAO.getVersmentById(versmentId);
        if (versment == null) {
            throw new IllegalArgumentException("Versment not found");
        }
        
        boolean success = versmentDAO.deleteVersmentById(versmentId);
        
        if (success) {
            // Add back the versment amount to client's remaining balance
            updateClientRemainingBalanceAfterVersment(versment.getClientId(), versment.getMontant(), true);
        }
        
        return success;
    }

    // 🔎 6. Get versment by ID
    public Versment getVersmentById(int id) {
        return versmentDAO.getVersmentById(id);
    }

    // 💰 7. Get total versments amount by client ID
    public BigDecimal getTotalVersmentsByClientId(int clientId) {
        return versmentDAO.getTotalVersmentsByClientId(clientId);
    }

    // 🔄 8. Update client's remaining balance after versment operations
    private void updateClientRemainingBalanceAfterVersment(int clientId, BigDecimal versmentAmount, boolean isRestore) {
        try {
            Client client = clientDAO.getClientById(clientId);
            if (client != null) {
                // Get current remaining balance, default to annual montant if not set
                BigDecimal currentRemainingBalance;
                if (client.getRemainingBalance() != null) {
                    currentRemainingBalance = BigDecimal.valueOf(client.getRemainingBalance());
                } else {
                    // If remaining balance is not set, initialize it with the annual montant
                    currentRemainingBalance = client.getMontant() != null ? 
                        BigDecimal.valueOf(client.getMontant()) : BigDecimal.ZERO;
                }
                
                BigDecimal newRemainingBalance;
                
                if (isRestore) {
                    // Restore: add back the versment amount to remaining balance
                    newRemainingBalance = currentRemainingBalance.add(versmentAmount);
                } else {
                    // Reduce: subtract the versment amount from remaining balance
                    newRemainingBalance = currentRemainingBalance.subtract(versmentAmount);
                    // Allow negative remaining balance (overpayment)
                }
                
                // Update the remaining balance in the database
                boolean updateSuccess = clientDAO.updateRemainingBalance(clientId, newRemainingBalance.doubleValue());
                
                if (updateSuccess) {
                    System.out.println("Updated client " + clientId + " remaining balance from " + 
                        currentRemainingBalance + " to " + newRemainingBalance + " (versment: " + versmentAmount + 
                        ", restore: " + isRestore + ")");
                } else {
                    System.err.println("Failed to update remaining balance for client " + clientId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating client remaining balance after versment operation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 🔄 9. Initialize remaining balance for clients that don't have it set
    public void initializeRemainingBalanceForClient(int clientId) {
        try {
            Client client = clientDAO.getClientById(clientId);
            if (client != null && client.getRemainingBalance() == null) {
                // Initialize remaining balance = annual montant - total versments
                BigDecimal annualMontant = client.getMontant() != null ? 
                    BigDecimal.valueOf(client.getMontant()) : BigDecimal.ZERO;
                BigDecimal totalVersments = getTotalVersmentsByClientId(clientId);
                BigDecimal remainingBalance = annualMontant.subtract(totalVersments);
                
                // Update the remaining balance
                boolean success = clientDAO.updateRemainingBalance(clientId, remainingBalance.doubleValue());
                if (success) {
                    System.out.println("Initialized remaining balance for client " + clientId + 
                        ": " + remainingBalance + " DA");
                } else {
                    System.err.println("Failed to initialize remaining balance for client " + clientId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing remaining balance for client: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    // 🔄 10. Initialize all clients' remaining balances
    public void initializeAllRemainingBalances() {
        try {
            com.yourcompany.clientmanagement.controller.ClientController clientController = 
                new com.yourcompany.clientmanagement.controller.ClientController();
            List<com.yourcompany.clientmanagement.model.Client> clients = clientController.fetchAllClients();
            
            for (com.yourcompany.clientmanagement.model.Client client : clients) {
                if (client.getRemainingBalance() == null) {
                    initializeRemainingBalanceForClient(client.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing all remaining balances: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // 💰 11. Get remaining amount for a client
    public BigDecimal getRemainingAmountForClient(int clientId) {
        try {
            Client client = clientDAO.getClientById(clientId);
            if (client == null) {
                return BigDecimal.ZERO;
            }
            
            // If remaining balance is set, use it
            if (client.getRemainingBalance() != null) {
                return BigDecimal.valueOf(client.getRemainingBalance());
            }
            
            // Otherwise calculate it (fallback for legacy data) - start with annual amount
            BigDecimal annualMontant = client.getMontant() != null ? 
                BigDecimal.valueOf(client.getMontant()) : BigDecimal.ZERO;
            BigDecimal totalVersments = getTotalVersmentsByClientId(clientId);
            return annualMontant.subtract(totalVersments);
        } catch (Exception e) {
            System.err.println("Error calculating remaining amount for client: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}