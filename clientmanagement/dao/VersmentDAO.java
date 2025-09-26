package com.yourcompany.clientmanagement.dao;

import com.yourcompany.clientmanagement.model.Versment;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VersmentDAO {

    // Get all versments
    public List<Versment> getAllVersments() {
        List<Versment> versments = new ArrayList<>();
        String sql = "SELECT * FROM versement ORDER BY date_paiement DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                versments.add(mapResultSetToVersment(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching versments: " + e.getMessage());
        }

        return versments;
    }

    // Get versments by client ID
    public List<Versment> getVersmentsByClientId(int clientId) {
        List<Versment> versments = new ArrayList<>();
        String sql = "SELECT * FROM versement WHERE client_id = ? ORDER BY date_paiement DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    versments.add(mapResultSetToVersment(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching versements by client ID: " + e.getMessage());
        }

        return versments;
    }

    // Insert new versment and return the generated ID
    public int insertVersment(Versment versment) {
        String sql = "INSERT INTO versement (client_id, montant, type, date_paiement, annee_concernee, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, versment.getClientId());
            stmt.setBigDecimal(2, versment.getMontant());
            stmt.setString(3, versment.getType());
            stmt.setDate(4, Date.valueOf(versment.getDatePaiement()));
            stmt.setString(5, versment.getAnneeConcernee());
            stmt.setTimestamp(6, Timestamp.valueOf(versment.getCreatedAt()));

            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating versment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating versment failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error inserting versment: " + e.getMessage());
            return -1;
        }
    }

    // Update versment
    public boolean updateVersment(Versment versment) {
        String sql = "UPDATE versement SET client_id=?, montant=?, type=?, date_paiement=?, annee_concernee=? "
                + "WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, versment.getClientId());
            stmt.setBigDecimal(2, versment.getMontant());
            stmt.setString(3, versment.getType());
            stmt.setDate(4, Date.valueOf(versment.getDatePaiement()));
            stmt.setString(5, versment.getAnneeConcernee());
            stmt.setInt(6, versment.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating versment: " + e.getMessage());
            return false;
        }
    }

    // Delete versment
    public boolean deleteVersmentById(int id) {
        String sql = "DELETE FROM versement WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting versment: " + e.getMessage());
            return false;
        }
    }

    // Get versment by ID
    public Versment getVersmentById(int id) {
        String sql = "SELECT * FROM versement WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVersment(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching versment by ID: " + e.getMessage());
        }
        
        return null;
    }

    // Get total versments amount by client ID
    public BigDecimal getTotalVersmentsByClientId(int clientId) {
        String sql = "SELECT SUM(montant) as total FROM versement WHERE client_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, clientId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error calculating total versments: " + e.getMessage());
        }
        
        return BigDecimal.ZERO;
    }

    // Map ResultSet to Versment object
    private Versment mapResultSetToVersment(ResultSet rs) throws SQLException {
        Versment versment = new Versment();
        versment.setId(rs.getInt("id"));
        versment.setClientId(rs.getInt("client_id"));
        versment.setMontant(rs.getBigDecimal("montant"));
        versment.setType(rs.getString("type"));
        
        Date datePaiement = rs.getDate("date_paiement");
        if (datePaiement != null) {
            versment.setDatePaiement(datePaiement.toLocalDate());
        }
        
        versment.setAnneeConcernee(rs.getString("annee_concernee"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            versment.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return versment;
    }
}