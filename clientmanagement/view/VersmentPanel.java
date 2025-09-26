package com.yourcompany.clientmanagement.view;

import com.yourcompany.clientmanagement.controller.VersmentController;
import com.yourcompany.clientmanagement.controller.ClientController;
import com.yourcompany.clientmanagement.model.Versment;
import com.yourcompany.clientmanagement.model.Client;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class VersmentPanel extends JPanel {
    private JTable versmentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JTextField minAmountField, maxAmountField;
    private JXDatePicker fromDatePicker, toDatePicker;
    private JComboBox<ClientItem> clientFilterCombo;
    private VersmentController versmentController;
    private ClientController clientController;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel totalLabel;
    private JPopupMenu contextMenu;
    private List<Versment> allVersments; // Store all versments for filtering

    public VersmentPanel() {
        versmentController = new VersmentController();
        clientController = new ClientController();
        initializeUI();
        setupTable();
        setupFilters();
        setupButtons();
        setupContextMenu();
        loadVersmentData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void setupTable() {
        // Table columns
        String[] columnNames = {
                "ID", "Client ID", "Nom Client", "Montant", "Type",
                "Date Paiement", "Année Concernée", "Date Création"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        versmentTable = new JTable(tableModel);
        customizeTableAppearance();
        setupColumnWidths();

        // Hide ID and Client ID columns
        versmentTable.removeColumn(versmentTable.getColumnModel().getColumn(0)); // Remove ID column
        versmentTable.removeColumn(versmentTable.getColumnModel().getColumn(0)); // Remove Client ID column (now at index 0)

        JScrollPane scrollPane = new JScrollPane(versmentTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void customizeTableAppearance() {
        Font tableFont = new Font("Segoe UI Emoji", Font.PLAIN, 14);
    Font headerFont = new Font("Segoe UI Emoji", Font.BOLD, 14);

        versmentTable.setFillsViewportHeight(true);
        versmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        versmentTable.setFont(tableFont);
        versmentTable.setRowHeight(30);
        versmentTable.getTableHeader().setFont(headerFont);
        versmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        versmentTable.setShowGrid(true);
        versmentTable.setGridColor(new Color(220, 220, 220));
        versmentTable.setIntercellSpacing(new Dimension(0, 1));

        versmentTable.setSelectionBackground(new Color(52, 152, 219));
        versmentTable.setSelectionForeground(Color.WHITE);
    }

    private void setupColumnWidths() {
        TableColumnModel columnModel = versmentTable.getColumnModel();

        // Adjust column indices since ID and Client ID are hidden
        columnModel.getColumn(0).setPreferredWidth(200); // Nom Client (now index 0)
        columnModel.getColumn(1).setPreferredWidth(100); // Montant (now index 1)
        columnModel.getColumn(2).setPreferredWidth(120); // Type (now index 2)
        columnModel.getColumn(3).setPreferredWidth(120); // Date Paiement (now index 3)
        columnModel.getColumn(4).setPreferredWidth(120); // Année Concernée (now index 4)
        columnModel.getColumn(5).setPreferredWidth(150); // Date Création (now index 5)
    }

    private void setupFilters() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            "🔍 Filtres de Recherche",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(52, 152, 219)
        ));
        filterPanel.setBackground(new Color(248, 249, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 1: Search and Client filter
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel searchLabel = new JLabel("🔍 Recherche générale:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterPanel.add(searchLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        filterPanel.add(searchField, gbc);

        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel clientLabel = new JLabel("👤 Client:");
        clientLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterPanel.add(clientLabel, gbc);

        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        clientFilterCombo = new JComboBox<>();
        clientFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clientFilterCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        

        ));
        loadClientFilter();
        filterPanel.add(clientFilterCombo, gbc);

        // Row 2: Amount range
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel amountLabel = new JLabel("💰 Montant:");
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterPanel.add(amountLabel, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        amountPanel.setBackground(new Color(248, 249, 250));
        
        JLabel fromAmountLabel = new JLabel("De:");
        fromAmountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        minAmountField = new JTextField(8);
        minAmountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addNumericValidation(minAmountField);
        
        JLabel toAmountLabel = new JLabel("À:");
        toAmountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        maxAmountField = new JTextField(8);
        maxAmountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addNumericValidation(maxAmountField);
        
        JLabel daLabel = new JLabel("DA");
        daLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        daLabel.setForeground(new Color(46, 125, 50));
        
        amountPanel.add(fromAmountLabel);
        amountPanel.add(minAmountField);
        amountPanel.add(toAmountLabel);
        amountPanel.add(maxAmountField);
        amountPanel.add(daLabel);
        filterPanel.add(amountPanel, gbc);

        // Row 2: Date range (continue on same row)
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel dateLabel = new JLabel("📅 Période:");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterPanel.add(dateLabel, gbc);

        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        datePanel.setBackground(new Color(248, 249, 250));
        
        JLabel fromDateLabel = new JLabel("Du:");
        fromDateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fromDatePicker = new JXDatePicker();
        fromDatePicker.setFormats("dd/MM/yyyy");
        fromDatePicker.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JLabel toDateLabel = new JLabel("Au:");
        toDateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toDatePicker = new JXDatePicker();
        toDatePicker.setFormats("dd/MM/yyyy");
        toDatePicker.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        datePanel.add(fromDateLabel);
        datePanel.add(fromDatePicker);
        datePanel.add(toDateLabel);
        datePanel.add(toDatePicker);
        filterPanel.add(datePanel, gbc);

        // Row 3: Action buttons and total
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setBackground(new Color(248, 249, 250));
        
        JButton applyFilterButton = new JButton("🔍 Appliquer");
        applyFilterButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        applyFilterButton.setBackground(new Color(52, 152, 219));
        applyFilterButton.setForeground(Color.WHITE);
        applyFilterButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        applyFilterButton.setFocusPainted(false);
        applyFilterButton.addActionListener(e -> applyFilters());
        
        JButton clearFilterButton = new JButton("🗑️ Effacer");
        clearFilterButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearFilterButton.setBackground(new Color(108, 117, 125));
        clearFilterButton.setForeground(Color.WHITE);
        clearFilterButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        clearFilterButton.setFocusPainted(false);
        clearFilterButton.addActionListener(e -> clearFilters());
        
        actionPanel.add(applyFilterButton);
        actionPanel.add(clearFilterButton);
        filterPanel.add(actionPanel, gbc);

        gbc.gridx = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        totalLabel = new JLabel("💰 Total des versements: 0.00 DA");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(new Color(46, 125, 50));
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        filterPanel.add(totalLabel, gbc);

        add(filterPanel, BorderLayout.NORTH);

        // Add listeners for real-time filtering
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
        
        clientFilterCombo.addActionListener(e -> applyFilters());
        
        // Add listeners for amount fields
        minAmountField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
        
        maxAmountField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
        
        // Add listeners for date pickers
        fromDatePicker.addActionListener(e -> applyFilters());
        toDatePicker.addActionListener(e -> applyFilters());
    }

    private void loadClientFilter() {
        try {
            List<Client> clients = clientController.fetchAllClients();
            
            // Add "All clients" option
            clientFilterCombo.addItem(new ClientItem(null, "-- Tous les clients --"));
            
            // Add individual clients
            for (Client client : clients) {
                clientFilterCombo.addItem(new ClientItem(client, null));
            }
        } catch (Exception e) {
            System.err.println("Error loading clients for filter: " + e.getMessage());
        }
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

    private void applyFilters() {
        if (allVersments == null) return;
        
        List<Versment> filteredVersments = new ArrayList<>(allVersments);
        
        // Apply search filter
        String searchText = searchField.getText().trim().toLowerCase();
        if (!searchText.isEmpty()) {
            filteredVersments = filteredVersments.stream()
                .filter(v -> {
                    Client client = clientController.getClientById(v.getClientId());
                    String clientName = client != null ? 
                        (client.getNom() + " " + (client.getPrenom() != null ? client.getPrenom() : "")).toLowerCase() : "";
                    return clientName.contains(searchText) || 
                           v.getType().toLowerCase().contains(searchText) ||
                           v.getAnneeConcernee().toLowerCase().contains(searchText);
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Apply client filter
        ClientItem selectedClient = (ClientItem) clientFilterCombo.getSelectedItem();
        if (selectedClient != null && selectedClient.getClient() != null) {
            int clientId = selectedClient.getClient().getId();
            filteredVersments = filteredVersments.stream()
                .filter(v -> v.getClientId() == clientId)
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Apply amount range filter
        String minAmountText = minAmountField.getText().trim();
        String maxAmountText = maxAmountField.getText().trim();
        
        if (!minAmountText.isEmpty()) {
            try {
                BigDecimal minAmount = new BigDecimal(minAmountText);
                filteredVersments = filteredVersments.stream()
                    .filter(v -> v.getMontant().compareTo(minAmount) >= 0)
                    .collect(java.util.stream.Collectors.toList());
            } catch (NumberFormatException e) {
                // Invalid number - ignore filter
            }
        }
        
        if (!maxAmountText.isEmpty()) {
            try {
                BigDecimal maxAmount = new BigDecimal(maxAmountText);
                filteredVersments = filteredVersments.stream()
                    .filter(v -> v.getMontant().compareTo(maxAmount) <= 0)
                    .collect(java.util.stream.Collectors.toList());
            } catch (NumberFormatException e) {
                // Invalid number - ignore filter
            }
        }
        
        // Apply date range filter
        Date fromDate = fromDatePicker.getDate();
        Date toDate = toDatePicker.getDate();
        
        if (fromDate != null) {
            LocalDate fromLocalDate = fromDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            filteredVersments = filteredVersments.stream()
                .filter(v -> v.getDatePaiement().isAfter(fromLocalDate) || v.getDatePaiement().isEqual(fromLocalDate))
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (toDate != null) {
            LocalDate toLocalDate = toDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            filteredVersments = filteredVersments.stream()
                .filter(v -> v.getDatePaiement().isBefore(toLocalDate) || v.getDatePaiement().isEqual(toLocalDate))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Update table with filtered results
        updateTableWithVersments(filteredVersments);
    }

    private void clearFilters() {
        searchField.setText("");
        minAmountField.setText("");
        maxAmountField.setText("");
        fromDatePicker.setDate(null);
        toDatePicker.setDate(null);
        clientFilterCombo.setSelectedIndex(0); // Select "All clients"
        
        // Show all versments
        if (allVersments != null) {
            updateTableWithVersments(allVersments);
        }
    }

    private void updateTableWithVersments(List<Versment> versments) {
        tableModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        
        for (Versment v : versments) {
            tableModel.addRow(convertVersmentToRow(v));
            if (v.getMontant() != null) {
                total = total.add(v.getMontant());
            }
        }
        
        totalLabel.setText("💰 Total des versements: " + total.toString() + " DA (" + versments.size() + " versements)");
    }

    private void setupButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton addButton = createButton("Ajouter Versement", e -> showAddDialog());
        JButton editButton = createButton("Modifier", e -> showEditDialog());
        JButton deleteButton = createButton("Supprimer", e -> deleteVersment());
        JButton refreshButton = createButton("Actualiser", e -> refreshVersmentTable());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupContextMenu() {
        contextMenu = new JPopupMenu();
        
        JMenuItem printReceiptItem = new JMenuItem("🖨️ Imprimer bon de versement");
        printReceiptItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        printReceiptItem.addActionListener(e -> printVersmentReceipt());
        
        JMenuItem viewDetailsItem = new JMenuItem("👁️ Voir détails");
        viewDetailsItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        viewDetailsItem.addActionListener(e -> showVersmentDetails());
        
        contextMenu.add(printReceiptItem);
        contextMenu.addSeparator();
        contextMenu.add(viewDetailsItem);
        
        // Add mouse listener to table for right-click
        versmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = versmentTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < versmentTable.getRowCount()) {
                        versmentTable.setRowSelectionInterval(row, row);
                    } else {
                        versmentTable.clearSelection();
                    }
                    contextMenu.show(versmentTable, e.getX(), e.getY());
                }
            }
        });
    }

    private void printVersmentReceipt() {
        int selectedRow = versmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un versement", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = versmentTable.convertRowIndexToModel(selectedRow);
        int versmentId = (Integer) tableModel.getValueAt(modelRow, 0); // ID is still at model index 0
        Versment versment = versmentController.getVersmentById(versmentId);

        if (versment != null) {
            Client client = clientController.getClientById(versment.getClientId());
            
            if (client != null) {
                // Show options dialog
                String[] options = {"🖨️ Imprimer directement", "👁️ Aperçu avant impression", "Annuler"};
                int choice = JOptionPane.showOptionDialog(this,
                    "Comment souhaitez-vous procéder?",
                    "Options d'impression",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);
                
                VersmentReceiptPrinter printer = new VersmentReceiptPrinter(versment, client);
                
                switch (choice) {
                    case 0: // Print directly
                        printer.printReceipt();
                        break;
                    case 1: // Show preview
                        printer.showPreview();
                        break;
                    // case 2 or default: Cancel - do nothing
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Impossible de trouver les informations du client", 
                    "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton createButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(150, 40));
        button.addActionListener(listener);
        return button;
    }

    private void loadVersmentData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                allVersments = versmentController.fetchAllVersments();
                SwingUtilities.invokeLater(() -> {
                    updateTableWithVersments(allVersments);
                });
                return null;
            }

            @Override
            protected void done() {
                versmentTable.repaint();
            }
        };
        worker.execute();
    }

    private Object[] convertVersmentToRow(Versment v) {
        // Get client name
        Client client = clientController.getClientById(v.getClientId());
        String clientName = client != null
                ? client.getNom() + " " + (client.getPrenom() != null ? client.getPrenom() : "")
                : "Client inconnu";

        return new Object[] {
                v.getId(),
                v.getClientId(),
                clientName,
                v.getMontant(),
                v.getType(),
                v.getDatePaiement() != null ? v.getDatePaiement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "",
                v.getAnneeConcernee(),
                v.getCreatedAt() != null ? v.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""
        };
    }

    private void refreshVersmentTable() {
        loadVersmentData();
    }

    private void showAddDialog() {
        VersmentDialog dialog = new VersmentDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Ajouter Versement",
                null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Versment newVersment = dialog.getVersment();
            int result = versmentController.addVersment(newVersment);
            if (result > 0) {
                refreshVersmentTable();
                JOptionPane.showMessageDialog(this, 
                    "Versement ajouté avec succès!\n" +
                    "Le montant annuel du client a été mis à jour automatiquement.");
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog() {
        int selectedRow = versmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un versement", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = versmentTable.convertRowIndexToModel(selectedRow);
        int versmentId = (Integer) tableModel.getValueAt(modelRow, 0); // ID is still at model index 0
        Versment versmentToEdit = versmentController.getVersmentById(versmentId);

        if (versmentToEdit != null) {
            VersmentDialog dialog = new VersmentDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Modifier Versement", versmentToEdit);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                Versment updatedVersment = dialog.getVersment();
                if (versmentController.updateVersment(updatedVersment)) {
                    refreshVersmentTable();
                    JOptionPane.showMessageDialog(this, 
                        "Versement modifié avec succès!\n" +
                        "Le montant annuel du client a été ajusté automatiquement.");
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur lors de la modification", "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteVersment() {
        int selectedRow = versmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un versement", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Êtes-vous sûr de vouloir supprimer ce versement?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int modelRow = versmentTable.convertRowIndexToModel(selectedRow);
            int versmentId = (Integer) tableModel.getValueAt(modelRow, 0); // ID is still at model index 0

            if (versmentController.deleteVersment(versmentId)) {
                refreshVersmentTable();
                JOptionPane.showMessageDialog(this, 
                    "Versement supprimé avec succès!\n" +
                    "Le montant annuel du client a été restauré automatiquement.");
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de la suppression", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showVersmentDetails() {
        int selectedRow = versmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un versement", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = versmentTable.convertRowIndexToModel(selectedRow);
        int versmentId = (Integer) tableModel.getValueAt(modelRow, 0); // ID is still at model index 0
        Versment versment = versmentController.getVersmentById(versmentId);

        if (versment != null) {
            Client client = clientController.getClientById(versment.getClientId());
            String clientName = client != null ? 
                (client.getNom() + (client.getPrenom() != null ? " " + client.getPrenom() : "")) : 
                "Client inconnu";
            
            String details = "📋 DÉTAILS DU VERSEMENT\n\n" +
                    "🆔 ID: " + versment.getId() + "\n" +
                    "👤 Client: " + clientName + "\n" +
                    "💰 Montant: " + versment.getMontant() + " DA\n" +
                    "📝 Type: " + versment.getType() + "\n" +
                    "📅 Date de paiement: " + versment.getDatePaiement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                    "📆 Année concernée: " + versment.getAnneeConcernee() + "\n" +
                    "🕒 Créé le: " + (versment.getCreatedAt() != null ? 
                        versment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) : "N/A");
            
            JOptionPane.showMessageDialog(this, details, "Détails du versement", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // Helper class for client filter combo box
    private static class ClientItem {
    private Client client;
    private String displayText;

    public ClientItem(Client client, String customText) {
        this.client = client;
        this.displayText = customText;
    }

    public Client getClient() {
        return client;
    }

    @Override
    public String toString() {
        if (displayText != null) {
            return displayText;
        }

        if (client == null) {
            return "-- Tous les clients --";
        }

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