package com.yourcompany.clientmanagement.model;

public class Client {
    private int id;
    private String nom;
    private String prenom;
    private String activite;
    private String annee;
    private String agentResponsable;
    private String formeJuridique;
    private String regimeFiscal;
    private String regimeCnas;
    private String modePaiement;
    private String indicateur;
    private String recetteImpots;
    private String observation;
    private Integer source;
    private String honorairesMois;
    private Double montant;
    private Double remainingBalance;
    private String phone;
    private String email;
    private String company;
    private String address;
    private String type;
    private String createdAt;
    private String updatedAt;
    private String premierVersement;
    private String n;
    private int code;

    // Getters
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getActivite() {
        return activite;
    }

    public String getAnnee() {
        return annee;
    }

    public String getAgentResponsable() {
        return agentResponsable;
    }

    public String getFormeJuridique() {
        return formeJuridique;
    }

    public String getRegimeFiscal() {
        return regimeFiscal;
    }

    public String getRegimeCnas() {
        return regimeCnas;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public String getIndicateur() {
        return indicateur;
    }

    public String getRecetteImpots() {
        return recetteImpots;
    }

    public String getObservation() {
        return observation;
    }

    public Integer getSource() {
        return source;
    }

    public String getHonorairesMois() {
        return honorairesMois;
    }

    public Double getMontant() {
        return montant;
    }
    

    public Double getRemainingBalance() {
        return remainingBalance;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getCompany() {
        return company;
    }

    public String getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getPremierVersement() {
        return premierVersement;
    }

    public String getN() {
        return n;
    }

    public int getCode() {
        return code;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setActivite(String activite) {
        this.activite = activite;
    }

    public void setAnnee(String annee) {
        this.annee = annee;
    }

    public void setAgentResponsable(String agentResponsable) {
        this.agentResponsable = agentResponsable;
    }

    public void setFormeJuridique(String formeJuridique) {
        this.formeJuridique = formeJuridique;
    }

    public void setRegimeFiscal(String regimeFiscal) {
        this.regimeFiscal = regimeFiscal;
    }

    public void setRegimeCnas(String regimeCnas) {
        this.regimeCnas = regimeCnas;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public void setIndicateur(String indicateur) {
        this.indicateur = indicateur;
    }

    public void setRecetteImpots(String recetteImpots) {
        this.recetteImpots = recetteImpots;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public void setHonorairesMois(String honorairesMois) {
        this.honorairesMois = honorairesMois;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }
   

    public void setRemainingBalance(Double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setPremierVersement(String premierVersement) {
        this.premierVersement = premierVersement;
    }

    public void setN(String n) {
        this.n = n;
    }

    public void setCode(int code) {
        this.code = code;
    }

    // Optional: toString() method for debugging
    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", activite='" + activite + '\'' +
                // ... include other fields as needed ...
                '}';
    }
}