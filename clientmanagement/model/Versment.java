package com.yourcompany.clientmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Versment {
    private int id;
    private int clientId;
    private BigDecimal montant;
    private String type;
    private LocalDate datePaiement;
    private String anneeConcernee;
    private LocalDateTime createdAt;

    // Default constructor
    public Versment() {}

    // Constructor with parameters
    public Versment(int clientId, BigDecimal montant, String type, LocalDate datePaiement, String anneeConcernee) {
        this.clientId = clientId;
        this.montant = montant;
        this.type = type;
        this.datePaiement = datePaiement;
        this.anneeConcernee = anneeConcernee;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public String getType() {
        return type;
    }

    public LocalDate getDatePaiement() {
        return datePaiement;
    }

    public String getAnneeConcernee() {
        return anneeConcernee;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDatePaiement(LocalDate datePaiement) {
        this.datePaiement = datePaiement;
    }

    public void setAnneeConcernee(String anneeConcernee) {
        this.anneeConcernee = anneeConcernee;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Versment{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", montant=" + montant +
                ", type='" + type + '\'' +
                ", datePaiement=" + datePaiement +
                ", anneeConcernee='" + anneeConcernee + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}