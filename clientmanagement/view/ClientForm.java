package com.yourcompany.clientmanagement.view;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdesktop.swingx.JXDatePicker;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// Excel-related imports
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.File;

import com.yourcompany.clientmanagement.controller.ClientController;
import com.yourcompany.clientmanagement.model.Client;
import com.yourcompany.clientmanagement.controller.VersmentController;
import java.math.BigDecimal;

public class ClientForm extends JFrame {
    // Table and data components
    private JTable clientTable;
    private DefaultTableModel tableModel;
    private ClientController controller;
    private TableRowSorter<DefaultTableModel> sorter;

    // Filter components
    private JTextField searchField;
    private JTextField minAmountField, maxAmountField;
    private JXDatePicker fromDatePicker, toDatePicker;

    // UI state
    private boolean isDarkMode = false;
    private VersmentController versmentController;

    // Column index constants
    private static final int COL_ID = 0;
    private static final int COL_NOM = 1;
    private static final int COL_ACTIVITE = 2;
    private static final int COL_ANNEE = 3;
    private static final int COL_FORME_JURIDIQUE = 4;
    private static final int COL_REGIME_FISCAL = 5;
    private static final int COL_REGIME_CNAS = 6;
    private static final int COL_RECETTE_IMPOTS = 7;
    private static final int COL_OBSERVATION = 8;
    private static final int COL_SOURCE = 9;
    private static final int COL_HONORAIRES_MOIS = 10;
    private static final int COL_MONTANT = 11;
    private static final int COL_TELEPHONE = 12;
    private static final int COL_COMPANY = 13;
    private static final int COL_CREATED_AT = 14;

    public ClientForm() {
        FlatLightLaf.setup();
        controller = new ClientController();
        versmentController = new VersmentController();

        initializeUI();
        setupTable();
        add(createFilterPanel(), BorderLayout.NORTH);
        setupButtons();
        loadClientData();
    }

    private void initializeUI() {
        setTitle("Client Management");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private void setupTable() {
        String[] columnNames = {
                "ID", "Nom", "Activité", "Année",
                "Forme Juridique", "Régime Fiscal", "Régime CNAS",
                "Recette Impôts", "Observation", "Source",
                "Honoraires/Mois", "Montant Annual", "Montant Restant", "Téléphone", "Created At"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == COL_MONTANT)
                    return Double.class;
                if (columnIndex == COL_MONTANT + 1) // Montant Restant column
                    return String.class;
                if (columnIndex == COL_SOURCE)
                    return Integer.class;
                return String.class;
            }
        };

        clientTable = new JTable(tableModel);
        customizeTableAppearance();
        setupColumnWidths();

        // Initialize sorter
        sorter = new TableRowSorter<>(tableModel);
        clientTable.setRowSorter(sorter);

        // Hide ID column
        clientTable.removeColumn(clientTable.getColumnModel().getColumn(COL_ID));

        JScrollPane scrollPane = new JScrollPane(clientTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
        
        // Update column headers with totals after data is loaded
        updateColumnHeadersWithTotals();
    }

    private void customizeTableAppearance() {
        java.awt.Font tableFont = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
        java.awt.Font headerFont = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);

        clientTable.setFillsViewportHeight(true);
        clientTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        clientTable.setFont(tableFont);
        clientTable.setRowHeight(30);
        clientTable.getTableHeader().setFont(headerFont);
        clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientTable.setShowGrid(true);
        clientTable.setGridColor(new java.awt.Color(220, 220, 220));
        clientTable.setIntercellSpacing(new Dimension(0, 1));

        clientTable.setSelectionBackground(new java.awt.Color(52, 152, 219));
        clientTable.setSelectionForeground(java.awt.Color.WHITE);
    }

    private void setupColumnWidths() {
        TableColumnModel columnModel = clientTable.getColumnModel();

        columnModel.getColumn(COL_NOM - 1).setPreferredWidth(150); // Adjust for hidden ID column
        columnModel.getColumn(COL_ACTIVITE - 1).setPreferredWidth(200);
        columnModel.getColumn(COL_ANNEE - 1).setPreferredWidth(80);
        columnModel.getColumn(COL_MONTANT).setPreferredWidth(120); // Montant Annual
        columnModel.getColumn(COL_MONTANT + 1).setPreferredWidth(120); // Montant Restant

        for (int i = COL_FORME_JURIDIQUE - 1; i < columnModel.getColumnCount(); i++) {
            if (i != COL_MONTANT && i != COL_MONTANT + 1) {
                columnModel.getColumn(i).setPreferredWidth(120);
            }
        }
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Search Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("recherche:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        searchField = new JTextField(20);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
        filterPanel.add(searchField, gbc);

        // Amount Range
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        filterPanel.add(new JLabel("montant anuuel:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        minAmountField = new JTextField(6);
        maxAmountField = new JTextField(6);
        amountPanel.add(new JLabel("du:"));
        amountPanel.add(minAmountField);
        amountPanel.add(new JLabel("a:"));
        amountPanel.add(maxAmountField);
        filterPanel.add(amountPanel, gbc);

        // Date Range
        gbc.gridx = 0;
        gbc.gridy = 2;
        filterPanel.add(new JLabel("Created Between:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        fromDatePicker = new JXDatePicker();
        toDatePicker = new JXDatePicker();
        fromDatePicker.setFormats("yyyy-MM-dd");
        toDatePicker.setFormats("yyyy-MM-dd");
        datePanel.add(fromDatePicker);
        datePanel.add(new JLabel("and"));
        datePanel.add(toDatePicker);
        filterPanel.add(datePanel, gbc);

        // Action Buttons
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton applyFilterButton = new JButton("Apply Filters");
        JButton clearFilterButton = new JButton("Clear Filters");

        buttonPanel.add(applyFilterButton);
        buttonPanel.add(clearFilterButton);
        filterPanel.add(buttonPanel, gbc);

        // Add action listeners
        applyFilterButton.addActionListener(e -> applyFilters());

        clearFilterButton.addActionListener(e -> {
            searchField.setText("");
            minAmountField.setText("");
            maxAmountField.setText("");
            fromDatePicker.setDate(null);
            toDatePicker.setDate(null);
            sorter.setRowFilter(null);
        });

        return filterPanel;
    }

    private void applyFilters() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Text Search Filter
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.orFilter(Arrays.asList(
                    RowFilter.regexFilter("(?i)" + searchText, COL_NOM),
                    RowFilter.regexFilter("(?i)" + searchText, COL_ACTIVITE),
                    RowFilter.regexFilter("(?i)" + searchText, COL_COMPANY))));
        }

        // Amount Range Filter
        try {
            if (!minAmountField.getText().isEmpty()) {
                double min = Double.parseDouble(minAmountField.getText());
                filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, min, COL_MONTANT));
            }
            if (!maxAmountField.getText().isEmpty()) {
                double max = Double.parseDouble(maxAmountField.getText());
                filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, max, COL_MONTANT));
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numbers for amount range",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Date Range Filter
        Date fromDate = fromDatePicker.getDate();
        Date toDate = toDatePicker.getDate();
        if (fromDate != null || toDate != null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        String dateStr = (String) entry.getValue(COL_CREATED_AT);
                        Date recordDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);

                        return (fromDate == null || recordDate.after(fromDate)) &&
                                (toDate == null || recordDate.before(toDate));
                    } catch (Exception ex) {
                        return false;
                    }
                }
            });
        }

        // Apply combined filter
        if (!filters.isEmpty()) {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        } else {
            sorter.setRowFilter(null);
        }
        
        // Update column headers with totals after filtering
        SwingUtilities.invokeLater(() -> updateColumnHeadersWithTotals());
    }

    private void setupButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        JButton addButton = createButton("Ajouter client", e -> showAddDialog());
        JButton editButton = createButton("Modifier client", e -> showEditDialog());
        JButton deleteButton = createButton("Supprimer client", e -> deleteClient());
        JButton refreshButton = createButton("Actualiser", e -> refreshClientTable());
        JButton columnButton = createButton("Colonnes", e -> showColumnSelector());
        JButton exportButton = createButton("Exporter Excel", e -> exportToExcel());
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(columnButton);
        buttonPanel.add(exportButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        button.setPreferredSize(new Dimension(150, 40));
        button.addActionListener(listener);
        return button;
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Spécifier où enregistrer le fichier Excel");
        fileChooser.setSelectedFile(new File("clients.xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSaveOriginal = fileChooser.getSelectedFile();
        final File fileToSaveFinal;
        if (!fileToSaveOriginal.getName().toLowerCase().endsWith(".xlsx")) {
            fileToSaveFinal = new File(fileToSaveOriginal.getParentFile(), fileToSaveOriginal.getName() + ".xlsx");
        } else {
            fileToSaveFinal = fileToSaveOriginal;
        }

        // Confirm overwrite if file exists
        if (fileToSaveFinal.exists()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Le fichier existe déjà. Voulez-vous le remplacer?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Execute export in background thread
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    exportVisibleDataToExcel(fileToSaveFinal);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(ClientForm.this,
                                "Exportation réussie!\nFichier enregistré: " + fileToSaveFinal.getAbsolutePath(),
                                "Export Excel", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(ClientForm.this,
                                "Erreur lors de l'exportation: " + e.getMessage(),
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }
        };
        worker.execute();
    }

    private void updateClientRowInTable(Client updatedClient) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            int clientId = (Integer) tableModel.getValueAt(row, COL_ID);
            if (clientId == updatedClient.getId()) {
                // Update the row with the new client data
                Object[] newRowData = convertClientToRow(updatedClient);
                for (int col = 0; col < newRowData.length; col++) {
                    tableModel.setValueAt(newRowData[col], row, col);
                }
                break;
            }
        }
        // Update column headers with new totals
        updateColumnHeadersWithTotals();
        clientTable.repaint();
    }

    private void exportVisibleDataToExcel(File file) throws Exception {
        // Create Excel workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Clients");

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Create data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);

        // Get visible columns
        List<Integer> visibleColumns = new ArrayList<>();
        TableColumnModel columnModel = clientTable.getColumnModel();

        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            if (columnModel.getColumn(i).getWidth() > 0) {
                visibleColumns.add(i);
            }
        }

        // Create header row
        Row headerRow = sheet.createRow(0);
        int colIndex = 0;

        for (int columnIndex : visibleColumns) {
            String headerValue = clientTable.getColumnName(columnIndex);
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headerValue);
            cell.setCellStyle(headerStyle);
        }

        // Add data rows
        TableModel model = clientTable.getModel();
        int rowCount = model.getRowCount();

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Row dataRow = sheet.createRow(rowIndex + 1);
            colIndex = 0;

            for (int columnIndex : visibleColumns) {
                Object value = model.getValueAt(rowIndex, columnIndex);
                Cell cell = dataRow.createCell(colIndex++);

                if (value != null) {
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Date) {
                        cell.setCellValue((Date) value);
                        CellStyle dateStyle = workbook.createCellStyle();
                        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));
                        cell.setCellStyle(dateStyle);
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
                cell.setCellStyle(dataStyle);
            }
        }

        // Auto-size columns
        for (int i = 0; i < visibleColumns.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        }

        workbook.close();
    }

    private void showColumnSelector() {
        JDialog columnDialog = new JDialog(this, "Sélection des colonnes", true);
        columnDialog.setSize(600, 400);
        columnDialog.setLocationRelativeTo(this);
        columnDialog.setLayout(new BorderLayout());

        // Create panel for checkboxes
        JPanel checkPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        checkPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Map to store checkbox and column index relationship
        Map<JCheckBox, Integer> columnMap = new HashMap<>();

        // Get the table column model (accounting for hidden ID column)
        TableColumnModel columnModel = clientTable.getColumnModel();

        // Create checkboxes for each visible column
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            String columnName = (String) column.getHeaderValue();
            JCheckBox checkBox = new JCheckBox(columnName, column.getWidth() > 0);
            columnMap.put(checkBox, i);
            checkPanel.add(checkBox);
        }

        JScrollPane scrollPane = new JScrollPane(checkPanel);
        columnDialog.add(scrollPane, BorderLayout.CENTER);

        // Add buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton selectAllButton = new JButton("Tout sélectionner");
        JButton deselectAllButton = new JButton("Tout désélectionner");
        JButton applyButton = new JButton("Appliquer");
        JButton cancelButton = new JButton("Annuler");

        buttonPanel.add(selectAllButton);
        buttonPanel.add(deselectAllButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        columnDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        selectAllButton.addActionListener(e -> {
            for (Component comp : checkPanel.getComponents()) {
                if (comp instanceof JCheckBox) {
                    ((JCheckBox) comp).setSelected(true);
                }
            }
        });

        deselectAllButton.addActionListener(e -> {
            for (Component comp : checkPanel.getComponents()) {
                if (comp instanceof JCheckBox) {
                    ((JCheckBox) comp).setSelected(false);
                }
            }
        });

        applyButton.addActionListener(e -> {
            for (Map.Entry<JCheckBox, Integer> entry : columnMap.entrySet()) {
                JCheckBox checkBox = entry.getKey();
                int columnIndex = entry.getValue();
                TableColumn column = columnModel.getColumn(columnIndex);

                if (checkBox.isSelected()) {
                    // Restore original width if previously hidden
                    if (column.getWidth() == 0) {
                        column.setPreferredWidth(100); // Default width
                    }
                } else {
                    // Hide column by setting width to 0
                    column.setPreferredWidth(0);
                    column.setWidth(0);
                    column.setMinWidth(0);
                    column.setMaxWidth(0);
                }
            }
            columnDialog.dispose();
        });

        cancelButton.addActionListener(e -> columnDialog.dispose());

        columnDialog.setVisible(true);
    }

    private void loadClientData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    List<Client> clients = controller.fetchAllClients();
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setRowCount(0);
                        for (Client c : clients) {
                            tableModel.addRow(convertClientToRow(c));
                        }
                        // Update column headers with totals after loading data
                        updateColumnHeadersWithTotals();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(ClientForm.this,
                                "Erreur lors du chargement des données: " + e.getMessage(),
                                "Erreur",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                clientTable.repaint();
            }
        };
        worker.execute();
    }

    private Object[] convertClientToRow(Client c) {
        // Use the remaining balance from the client object if available
        // Otherwise calculate it from versments
        String remainingAmountStr;

        if (c.getRemainingBalance() != null) {
            // Use the stored remaining balance
            remainingAmountStr = c.getRemainingBalance() + " DA";
        } else {
            // Calculate from versments as fallback
            BigDecimal remainingAmount = versmentController.getRemainingAmountForClient(c.getId());
            remainingAmountStr = remainingAmount.toString() + " DA";
        }

        // Add color coding info
       
        

        return new Object[] {
                c.getId(), c.getNom(), c.getActivite(), c.getAnnee(),
                c.getFormeJuridique(), c.getRegimeFiscal(), c.getRegimeCnas(),
                c.getRecetteImpots(), c.getObservation(), c.getSource(),
                c.getHonorairesMois(), c.getMontant(), remainingAmountStr, c.getPhone(), c.getCreatedAt()
        };
    }

    private void refreshClientTable() {
        loadClientData();
    }
    
    private void updateColumnHeadersWithTotals() {
        try {
            // Calculate totals from current table data
            int totalClients = tableModel.getRowCount();
            double totalHonorairesMois = 0.0;
            double totalMontantAnnual = 0.0;
            double totalRemainingBalance = 0.0;
            
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                // Honoraires/Mois (column index 10 in model)
                Object honorairesObj = tableModel.getValueAt(row, COL_HONORAIRES_MOIS);
                if (honorairesObj != null && !honorairesObj.toString().isEmpty()) {
                    try {
                        totalHonorairesMois += Double.parseDouble(honorairesObj.toString());
                    } catch (NumberFormatException e) {
                        // Skip invalid values
                    }
                }
                
                // Montant Annual (column index 11 in model)
                Object montantObj = tableModel.getValueAt(row, COL_MONTANT);
                if (montantObj != null) {
                    if (montantObj instanceof Double) {
                        totalMontantAnnual += (Double) montantObj;
                    } else {
                        try {
                            totalMontantAnnual += Double.parseDouble(montantObj.toString());
                        } catch (NumberFormatException e) {
                            // Skip invalid values
                        }
                    }
                }
                
                // Remaining Balance (column index 12 in model) - extract numeric value from "X DA" format
                Object remainingObj = tableModel.getValueAt(row, COL_MONTANT + 1);
                if (remainingObj != null) {
                    String remainingStr = remainingObj.toString();
                    if (remainingStr.contains(" DA")) {
                        try {
                            String numericPart = remainingStr.replace(" DA", "").trim();
                            totalRemainingBalance += Double.parseDouble(numericPart);
                        } catch (NumberFormatException e) {
                            // Skip invalid values
                        }
                    }
                }
            }
            
            // Update column headers with totals
            TableColumnModel columnModel = clientTable.getColumnModel();
            
            // Update "Nom" column header with total clients (visible column index 0, since ID is hidden)
            TableColumn nomColumn = columnModel.getColumn(0);
            nomColumn.setHeaderValue("Nom (Total: " + totalClients + " clients)");
            // Set green color only for the total in the "Nom" column header, keep 'Nom' as it is
            String nomHeader = "<html>Nom (<span style='color:green;'>Total: " + totalClients + " clients</span>)</html>";
            nomColumn.setHeaderValue(nomHeader);
            nomColumn.setPreferredWidth(180);
            
            // Update "Honoraires/Mois" column header (visible column index 9, since ID is hidden)
            TableColumn honorairesColumn = columnModel.getColumn(9);
            String honorairesHeader = "<html>Honoraires/Mois (<span style='color:green;'>Total: " + String.format("%.2f", totalHonorairesMois) + " DA</span>)</html>";
            honorairesColumn.setHeaderValue(honorairesHeader);
            honorairesColumn.setPreferredWidth(260);

            // Update "Montant Annual" column header (visible column index 10, since ID is hidden)
            TableColumn montantColumn = columnModel.getColumn(10);
            String montantHeader = "<html>Montant Annual (<span style='color:green;'>Total: " + String.format("%.2f", totalMontantAnnual) + " DA</span>)</html>";
            montantColumn.setHeaderValue(montantHeader);
            montantColumn.setPreferredWidth(260);

            // Update "Montant Restant" column header (visible column index 11, since ID is hidden)
            TableColumn remainingColumn = columnModel.getColumn(11);
            String remainingHeader = "<html>Montant Restant (<span style='color:green;'>Total: " + String.format("%.2f", totalRemainingBalance) + " DA</span>)</html>";
            remainingColumn.setHeaderValue(remainingHeader);
            remainingColumn.setPreferredWidth(260);

            // Refresh the table header
            clientTable.getTableHeader().repaint();
            
        } catch (Exception e) {
            System.err.println("Error updating column headers with totals: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void restoreAllColumns() {
        TableColumnModel columnModel = clientTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            if (column.getWidth() == 0) {
                // Restore to default width
                column.setPreferredWidth(100);
                column.setMinWidth(15);
                column.setMaxWidth(Integer.MAX_VALUE);
            }
        }
    }

    private void showAddDialog() {
        ClientDialog dialog = new ClientDialog(this, "Ajouter Client", null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Client newClient = dialog.getClient();
            try {
                int result = controller.addClient(newClient);
                if (result > 0) {
                    refreshClientTable();
                    JOptionPane.showMessageDialog(this, "Client ajouté avec succès! ID: " + result);
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout du client", "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout: " + e.getMessage(), "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void refreshRemainingBalances() {
    SwingWorker<Void, Void> worker = new SwingWorker<>() {
        @Override
        protected Void doInBackground() {
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                int clientId = (Integer) tableModel.getValueAt(row, COL_ID);
                Client client = controller.getClientById(clientId);
                if (client != null) {
                    Object[] newRowData = convertClientToRow(client);
                    final int finalRow = row;
                    SwingUtilities.invokeLater(() -> {
                        // Update only the remaining balance column (column 12)
                        tableModel.setValueAt(newRowData[12], finalRow, 12);
                    });
                }
            }
            return null;
        }
        
        @Override
        protected void done() {
            clientTable.repaint();
        }
    };
    worker.execute();
}

    private void showEditDialog() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int modelRow = clientTable.convertRowIndexToModel(selectedRow);
            int clientId = (Integer) tableModel.getValueAt(modelRow, COL_ID);
            Client clientToEdit = controller.getClientById(clientId);

            if (clientToEdit != null) {
                ClientDialog dialog = new ClientDialog(this, "Modifier Client", clientToEdit);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    Client updatedClient = dialog.getClient();
                    if (controller.updateClient(updatedClient)) {
                        // Update just this row instead of refreshing the whole table
                        updateClientRowInTable(updatedClient);
                        JOptionPane.showMessageDialog(this, "Client modifié avec succès!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Erreur lors de la modification", "Erreur",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Client non trouvé", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la modification: " + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteClient() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client", "Avertissement",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Êtes-vous sûr de vouloir supprimer ce client?\nCette action supprimera également tous ses versements.",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int modelRow = clientTable.convertRowIndexToModel(selectedRow);
                int clientId = (Integer) tableModel.getValueAt(modelRow, COL_ID);

                if (controller.deleteClient(clientId)) {
                    refreshClientTable();
                    JOptionPane.showMessageDialog(this, "Client supprimé avec succès!");
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur lors de la suppression", "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur lors de la suppression: " + e.getMessage(), "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new ClientForm().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}