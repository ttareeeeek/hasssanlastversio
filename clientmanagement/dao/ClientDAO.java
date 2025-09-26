package com.yourcompany.clientmanagement.dao;

import com.yourcompany.clientmanagement.model.Client;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {
    private static final String SELECT_ALL = "SELECT id, nom, activite, annee, forme_juridique, " +
            "regime_fiscal, regime_cnas, recette_impots, observation, source, honoraires_mois, " +
            "(honoraires_mois * 12) as montant, remaining_balance, phone, company, " +
            "created_at, updated_at " +
            "FROM clients ORDER BY nom";

    private static final String UPDATE_REMAINING_BALANCE = "UPDATE clients SET remaining_balance=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";

    private static final String INSERT = "INSERT INTO clients (nom, activite, annee, " +
            "forme_juridique, regime_fiscal, regime_cnas, recette_impots, observation, " +
            "source, honoraires_mois, montant, remaining_balance, phone, company) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE = "UPDATE clients SET nom=?, activite=?, annee=?, " +
            "forme_juridique=?, regime_fiscal=?, regime_cnas=?, recette_impots=?, " +
            "observation=?, source=?, honoraires_mois=?, montant=?, remaining_balance=?, phone=?, company=?, " +
            "updated_at=CURRENT_TIMESTAMP WHERE id=?";

    private static final String DELETE = "DELETE FROM clients WHERE id=?";
    private static final String SELECT_BY_ID = "SELECT * FROM clients WHERE id = ?";
    private static final String SEARCH = "SELECT * FROM clients WHERE nom LIKE ? OR activite LIKE ? OR company LIKE ? ORDER BY nom";

    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            handleException("Error fetching clients", e);
        }
        return clients;
    }

    public boolean updateRemainingBalance(int clientId, Double remainingBalance) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_REMAINING_BALANCE)) {

            if (remainingBalance != null) {
                stmt.setBigDecimal(1, BigDecimal.valueOf(remainingBalance));
            } else {
                stmt.setNull(1, Types.DECIMAL);
            }
            stmt.setInt(2, clientId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("Error updating remaining balance", e);
            return false;
        }
    }

    public int insertClient(Client client) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            setCommonParameters(stmt, client);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating client failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating client failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            handleException("Error inserting client", e);
            return -1;
        }
    }

    public boolean updateClient(Client client) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE)) {

            setCommonParameters(stmt, client);
            stmt.setInt(15, client.getId()); // Changed from 14 to 15 to account for the added parameter

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("Error updating client", e);
            return false;
        }
    }

    public boolean deleteClientById(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(DELETE)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("Error deleting client", e);
            return false;
        }
    }

    public Client getClientById(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClient(rs);
                }
            }
        } catch (SQLException e) {
            handleException("Error fetching client by ID", e);
        }
        return null;
    }

    public List<Client> searchClients(String keyword) {
        List<Client> clients = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SEARCH)) {

            String likeKeyword = "%" + keyword + "%";
            stmt.setString(1, likeKeyword);
            stmt.setString(2, likeKeyword);
            stmt.setString(3, likeKeyword);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clients.add(mapResultSetToClient(rs));
                }
            }
        } catch (SQLException e) {
            handleException("Error searching clients", e);
        }
        return clients;
    }

    private void setCommonParameters(PreparedStatement stmt, Client client) throws SQLException {
        Double montantAnnual = calculateAnnualAmount(client.getHonorairesMois());

        stmt.setString(1, client.getNom());
        stmt.setString(2, client.getActivite());
        stmt.setString(3, client.getAnnee());
        stmt.setString(4, client.getFormeJuridique());
        stmt.setString(5, client.getRegimeFiscal());
        stmt.setString(6, client.getRegimeCnas());
        stmt.setString(7, client.getRecetteImpots());
        stmt.setString(8, client.getObservation());
        stmt.setObject(9, client.getSource(), Types.INTEGER);
        stmt.setString(10, client.getHonorairesMois());
        stmt.setObject(11, montantAnnual, Types.DECIMAL);

        // Set remaining_balance parameter - always initialize with annual amount if not explicitly set
        Double remainingBalance = client.getRemainingBalance();
        if (remainingBalance == null || remainingBalance == 0.0) {
            remainingBalance = montantAnnual;
        }
        stmt.setObject(12, remainingBalance, Types.DECIMAL);

        stmt.setString(13, client.getPhone());
        stmt.setString(14, client.getCompany());
    }

    private Double calculateAnnualAmount(String honorairesMois) {
        if (honorairesMois == null || honorairesMois.isEmpty()) {
            return null;
        }
        try {
            double monthlyAmount = Double.parseDouble(honorairesMois);
            return monthlyAmount * 12;
        } catch (NumberFormatException e) {
            System.err.println("Invalid honoraires_mois format: " + honorairesMois);
            return null;
        }
    }

    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setId(rs.getInt("id"));
        client.setNom(rs.getString("nom"));
        client.setActivite(rs.getString("activite"));
        client.setAnnee(rs.getString("annee"));
        client.setFormeJuridique(rs.getString("forme_juridique"));
        client.setRegimeFiscal(rs.getString("regime_fiscal"));
        client.setRegimeCnas(rs.getString("regime_cnas"));
        client.setRecetteImpots(rs.getString("recette_impots"));
        client.setObservation(rs.getString("observation"));
        client.setSource(rs.getObject("source") != null ? rs.getInt("source") : null);
        client.setHonorairesMois(rs.getString("honoraires_mois"));

        BigDecimal montantBD = rs.getBigDecimal("montant");
        client.setMontant(montantBD != null ? montantBD.doubleValue() : null);

        // Map remaining_balance
        BigDecimal remainingBalanceBD = rs.getBigDecimal("remaining_balance");
        client.setRemainingBalance(remainingBalanceBD != null ? remainingBalanceBD.doubleValue() : null);

        client.setPhone(rs.getString("phone"));
        client.setCompany(rs.getString("company"));
        client.setCreatedAt(rs.getString("created_at"));
        client.setUpdatedAt(rs.getString("updated_at"));

        return client;
    }

    private void handleException(String message, SQLException e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }
}