package com.yourcompany.clientmanagement.view;

import com.yourcompany.clientmanagement.controller.ClientController;
import com.yourcompany.clientmanagement.controller.VersmentController;
import com.yourcompany.clientmanagement.model.Client;
import com.yourcompany.clientmanagement.model.Versment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class VersmentDialog extends JDialog {
    private JComboBox<ClientItem> clientComboBox;
    private DefaultComboBoxModel<ClientItem> clientModel;
    private JTextField montantField;
    private JComboBox<String> typeComboBox;
    private JTextField datePaiementField;
    private JTextField anneeConcerneeField;
    private JLabel remainingAmountLabel;
    
    private boolean confirmed = false;
    private Versment versment;
    private VersmentController versmentController;
    private ClientController clientController;
    private List<ClientItem> allClientItems;

    public VersmentDialog(JFrame parent, String title, Versment versment) {
        super(parent, title, true);
        this.versment = versment;
        this.versmentController = new VersmentController();
        this.clientController = new ClientController();
        
        initializeUI();
        loadClients();
        populateFields();
    }

    private void initializeUI() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        // Main form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Client selection with search
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Client*:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        clientModel = new DefaultComboBoxModel<>();
        clientComboBox = new JComboBox<>(clientModel);
        clientComboBox.setEditable(true);
        clientComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Add search functionality to client combo
        JTextField clientEditor = (JTextField) clientComboBox.getEditor().getEditorComponent();
        clientEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterClients(clientEditor.getText());
            }
        });
        
        clientComboBox.addActionListener(e -> {
            Object selected = clientComboBox.getSelectedItem();
            if (selected instanceof ClientItem) {
                updateRemainingAmount(((ClientItem) selected).getClient().getId());
            }
        });
        
        formPanel.add(clientComboBox, gbc);

        // Remaining amount label
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        remainingAmountLabel = new JLabel("Montant restant: Sélectionnez un client");
        remainingAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        remainingAmountLabel.setForeground(new Color(46, 125, 50));
        formPanel.add(remainingAmountLabel, gbc);

        // Reset grid settings
        gbc.gridwidth = 1; gbc.weightx = 0;

        // Montant
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Montant*:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        montantField = new JTextField();
        montantField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addNumericValidation(montantField);
        
        // Add listener to update remaining amount preview
        montantField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateRemainingAmountPreview();
            }
        });
        
        formPanel.add(montantField, gbc);

        // Type
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Type*:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        typeComboBox = new JComboBox<>(new String[]{"Acompte", "Solde", "Paiement partiel", "Autre"});
        typeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(typeComboBox, gbc);

        // Date Paiement
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Date Paiement*:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        datePaiementField = new JTextField();
        datePaiementField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        datePaiementField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        formPanel.add(datePaiementField, gbc);

        // Année Concernée
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Année Concernée*:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        anneeConcerneeField = new JTextField();
        anneeConcerneeField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        anneeConcerneeField.setText(String.valueOf(LocalDate.now().getYear()));
        formPanel.add(anneeConcerneeField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 15, 10));

        JButton okButton = new JButton("Enregistrer");
        okButton.setPreferredSize(new Dimension(120, 35));
        okButton.addActionListener(e -> validateAndClose());

        JButton cancelButton = new JButton("Annuler");
        cancelButton.setPreferredSize(new Dimension(120, 35));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);
    }

    private void filterClients(String searchText) {
        if (allClientItems == null) return;
        
        clientModel.removeAllElements();
        
        if (searchText.trim().isEmpty()) {
            for (ClientItem item : allClientItems) {
                clientModel.addElement(item);
            }
        } else {
            // Filter based on search text
            List<ClientItem> filtered = allClientItems.stream()
                .filter(item -> item.toString().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
            
            for (ClientItem item : filtered) {
                clientModel.addElement(item);
            }
        }
        
        clientComboBox.setPopupVisible(true);
        ((JTextField)clientComboBox.getEditor().getEditorComponent()).setText(searchText);
    }

    private void addNumericValidation(JTextField field) {
        field.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!(Character.isDigit(c) || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE || c == '.')) {
                    e.consume();
                }
            }
        });
    }

    private void loadClients() {
        try {
            List<Client> clients = clientController.fetchAllClients();
            allClientItems = clients.stream()
                .map(ClientItem::new)
                .collect(Collectors.toList());
            
            for (ClientItem item : allClientItems) {
                clientModel.addElement(item);
            }
        } catch (Exception e) {
            System.err.println("Error loading clients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateFields() {
        if (versment == null) return;

        // Select the correct client
        for (int i = 0; i < clientComboBox.getItemCount(); i++) {
            ClientItem item = clientComboBox.getItemAt(i);
            if (item.getClient().getId() == versment.getClientId()) {
                clientComboBox.setSelectedIndex(i);
                updateRemainingAmount(item.getClient().getId());
                break;
            }
        }

        montantField.setText(versment.getMontant() != null ? versment.getMontant().toString() : "");
        typeComboBox.setSelectedItem(versment.getType());
        
        if (versment.getDatePaiement() != null) {
            datePaiementField.setText(versment.getDatePaiement().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        
        anneeConcerneeField.setText(versment.getAnneeConcernee());
    }

    private void updateRemainingAmount(int clientId) {
        if (remainingAmountLabel == null) {
            return; // Safety check
        }
        
        try {
            BigDecimal remaining = versmentController.getRemainingAmountForClient(clientId);
            remainingAmountLabel.setText("Montant restant: " + remaining.toString() + " DA");
            
            // Change color based on remaining amount
            if (remaining.compareTo(BigDecimal.ZERO) == 0) {
                remainingAmountLabel.setForeground(new Color(244, 67, 54)); // Red - fully paid
                remainingAmountLabel.setText("Montant restant: " + remaining.toString() + " DA (Entièrement payé)");
            } else if (remaining.compareTo(new BigDecimal("1000")) < 0) {
                remainingAmountLabel.setForeground(new Color(255, 152, 0)); // Orange - low remaining
            } else {
                remainingAmountLabel.setForeground(new Color(46, 125, 50)); // Green - good remaining
            }
        } catch (Exception e) {
            remainingAmountLabel.setText("Montant restant: Erreur de calcul");
            remainingAmountLabel.setForeground(Color.RED);
            System.err.println("Error updating remaining amount: " + e.getMessage());
        }
    }

    private void updateRemainingAmountPreview() {
        if (remainingAmountLabel == null) {
            return; // Safety check
        }
        
        Object selected = clientComboBox.getSelectedItem();
        if (selected instanceof ClientItem) {
            ClientItem selectedClient = (ClientItem) selected;
            try {
                BigDecimal currentRemaining = versmentController.getRemainingAmountForClient(selectedClient.getClient().getId());
                String amountText = montantField.getText().trim();
                
                if (!amountText.isEmpty()) {
                    BigDecimal newVersmentAmount = new BigDecimal(amountText);
                    BigDecimal afterVersment = currentRemaining.subtract(newVersmentAmount);
                    
                    if (afterVersment.compareTo(BigDecimal.ZERO) < 0) {
                        remainingAmountLabel.setText("Montant restant: " + currentRemaining.toString() + 
                            " DA → " + afterVersment.toString() + " DA (Dépassement!)");
                        remainingAmountLabel.setForeground(Color.RED);
                    } else {
                        remainingAmountLabel.setText("Montant restant: " + currentRemaining.toString() + 
                            " DA → " + afterVersment.toString() + " DA");
                        remainingAmountLabel.setForeground(new Color(46, 125, 50));
                    }
                } else {
                    updateRemainingAmount(selectedClient.getClient().getId());
                }
            } catch (NumberFormatException e) {
                updateRemainingAmount(selectedClient.getClient().getId());
            }
        }
    }

    private void validateAndClose() {
        // Validate required fields
        Object selected = clientComboBox.getSelectedItem();
        if (selected == null || (selected instanceof String && ((String)selected).isEmpty())) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client valide", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!(selected instanceof ClientItem)) {
            // Try to find matching client
            String searchText = selected.toString();
            for (ClientItem item : allClientItems) {
                if (item.toString().equalsIgnoreCase(searchText)) {
                    clientComboBox.setSelectedItem(item);
                    selected = item;
                    break;
                }
            }
            
            if (!(selected instanceof ClientItem)) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client valide", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if (montantField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le montant est obligatoire", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (typeComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Le type est obligatoire", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (datePaiementField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La date de paiement est obligatoire", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (anneeConcerneeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "L'année concernée est obligatoire", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate amount format
        try {
            new BigDecimal(montantField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Format de montant invalide", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate date format
        try {
            LocalDate.parse(datePaiementField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Format de date invalide (YYYY-MM-DD)", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate that versment amount doesn't exceed remaining amount (optional warning)
        if (selected instanceof ClientItem) {
            ClientItem selectedClient = (ClientItem) selected;
            try {
                BigDecimal currentRemaining = versmentController.getRemainingAmountForClient(selectedClient.getClient().getId());
                BigDecimal versmentAmount = new BigDecimal(montantField.getText().trim());
                
                if (versmentAmount.compareTo(currentRemaining) > 0) {
                    int choice = JOptionPane.showConfirmDialog(this,
                        "Le montant du versement (" + versmentAmount + " DA) dépasse le montant restant (" + 
                        currentRemaining + " DA).\n\nVoulez-vous continuer quand même?",
                        "Montant dépassé",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    
                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                // Amount validation will be handled above
            }
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Versment getVersment() {
        if (!confirmed) return null;

        Versment v = versment != null ? versment : new Versment();
        
        Object selected = clientComboBox.getSelectedItem();
        if (!(selected instanceof ClientItem)) {
            return null;
        }
        
        ClientItem selectedClient = (ClientItem) selected;
        v.setClientId(selectedClient.getClient().getId());
        v.setMontant(new BigDecimal(montantField.getText().trim()));
        v.setType((String) typeComboBox.getSelectedItem());
        v.setDatePaiement(LocalDate.parse(datePaiementField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        v.setAnneeConcernee(anneeConcerneeField.getText().trim());
        
        // Set creation time for new versments
        if (versment == null) {
            v.setCreatedAt(LocalDateTime.now());
        }

        return v;
    }

    // Helper class for client combo box items
    private static class ClientItem {
        private Client client;

        public ClientItem(Client client) {
            this.client = client;
        }

        public Client getClient() {
            return client;
        }

        @Override
        public String toString() {
            String name = client.getNom();
            if (client.getPrenom() != null && !client.getPrenom().trim().isEmpty()) {
                name += " " + client.getPrenom();
            }
            if (client.getCompany() != null && !client.getCompany().trim().isEmpty()) {
                name += " (" + client.getCompany() + ")";
            }
            return name;
        }
    }
}