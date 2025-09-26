-- Create database if not exists
CREATE DATABASE IF NOT EXISTS client_management;
USE client_management;

-- Create clients table
CREATE TABLE IF NOT EXISTS clients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100),
    activite VARCHAR(200) NOT NULL,
    annee VARCHAR(10),
    agent_responsable VARCHAR(100),
    forme_juridique VARCHAR(100),
    regime_fiscal VARCHAR(100),
    regime_cnas VARCHAR(100),
    mode_paiement VARCHAR(100),
    indicateur VARCHAR(100),
    recette_impots VARCHAR(100),
    observation TEXT,
    source INT,
    honoraires_mois VARCHAR(50),
    montant DECIMAL(15,2),
    phone VARCHAR(20),
    email VARCHAR(100),
    company VARCHAR(200),
    address TEXT,
    type VARCHAR(50),
    premier_versement VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create versments table
CREATE TABLE IF NOT EXISTS versments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_id INT NOT NULL,
    montant DECIMAL(15,2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    date_paiement DATE NOT NULL,
    annee_concernee VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_clients_nom ON clients(nom);
CREATE INDEX idx_clients_activite ON clients(activite);
CREATE INDEX idx_versments_client_id ON versments(client_id);
CREATE INDEX idx_versments_date_paiement ON versments(date_paiement);
CREATE INDEX idx_versments_annee_concernee ON versments(annee_concernee);