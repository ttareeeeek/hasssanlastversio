package com.yourcompany.clientmanagement.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.yourcompany.clientmanagement.controller.ClientController;
import com.yourcompany.clientmanagement.controller.VersmentController;
import com.yourcompany.clientmanagement.model.Client;
import com.yourcompany.clientmanagement.model.Versment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MainFrame extends JFrame {
    private boolean isDarkMode = false;
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    private JMenuItem themeToggleButton;
    private ClientController clientController;
    private VersmentController versmentController;
    
    // Dashboard components
    private JLabel totalClientsLabel;
    private JLabel totalVersmentsLabel;
    private JLabel monthlyRevenueLabel;
    private JLabel pendingAmountLabel;

    public MainFrame() {
        // Set initial theme
        FlatLightLaf.setup();
        
        // Initialize controllers
        clientController = new ClientController();
        versmentController = new VersmentController();

        // Initialize remaining balances for existing clients
        SwingUtilities.invokeLater(() -> {
            versmentController.initializeAllRemainingBalances();
        });
        initializeUI();
        setupTabs();
        setupMenuBar();
        setupStatusBar();
        
        // Show welcome message
        SwingUtilities.invokeLater(this::showWelcomeMessage);
    }

    private void initializeUI() {
        setTitle("💼 Système de Gestion - Clients & Versements");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Set application icon
        try {
            setIconImage(createIconImage());
        } catch (Exception e) {
            System.err.println("Could not load application icon");
        }
    }

    private Image createIconImage() {
        // Create a simple icon programmatically
        int size = 32;
        java.awt.image.BufferedImage icon = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a modern gradient background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(52, 152, 219), size, size, new Color(41, 128, 185));
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, size, size, 8, 8);
        
        // Draw a simple "C" for Client
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "C";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return icon;
    }

    private void setupTabs() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        
        // Modern tab styling
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        // Client management tab
        ClientForm clientForm = new ClientForm();
        clientForm.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        clientForm.setVisible(false);

        JPanel clientPanel = (JPanel) clientForm.getContentPane();
        tabbedPane.addTab(" Gestion des Clients", createTabIcon("👥"), clientPanel, "Gérer les informations clients");

        // Versment management tab
        VersmentPanel versmentPanel = new VersmentPanel();
        tabbedPane.addTab(" Gestion des Versements", createTabIcon("💰"), versmentPanel, "Gérer les versements et paiements");

        // Dashboard tab (optional - you can add analytics here later)
        JPanel dashboardPanel = createDashboardPanel();
        tabbedPane.addTab(" Tableau de Bord", createTabIcon("📊"), dashboardPanel, "Vue d'ensemble et statistiques");

        add(tabbedPane, BorderLayout.CENTER);
    }

    private Icon createTabIcon(String emoji) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                g2d.drawString(emoji, x, y + 14);
                g2d.dispose();
            }

            @Override
            public int getIconWidth() { return 20; }

            @Override
            public int getIconHeight() { return 16; }
        };
    }

    private JPanel createDashboardPanel() {
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("📊 Tableau de Bord");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(52, 152, 219));
        headerPanel.add(titleLabel);
        
        // Add refresh button
        JButton refreshButton = new JButton("🔄 Actualiser");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setBackground(new Color(52, 152, 219));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> updateDashboardData());
        headerPanel.add(Box.createHorizontalStrut(20));
        headerPanel.add(refreshButton);
        
        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Create stats cards with labels that will be updated
        JPanel totalClientsCard = createStatsCard("👥", "Total Clients", "Chargement...", new Color(52, 152, 219));
        totalClientsLabel = findValueLabel(totalClientsCard);
        statsPanel.add(totalClientsCard);
        
        JPanel totalVersmentsCard = createStatsCard("💰", "Total Versements", "Chargement...", new Color(46, 125, 50));
        totalVersmentsLabel = findValueLabel(totalVersmentsCard);
        statsPanel.add(totalVersmentsCard);
        
        JPanel monthlyRevenueCard = createStatsCard("📈", "Revenus ce mois", "Chargement...", new Color(255, 152, 0));
        monthlyRevenueLabel = findValueLabel(monthlyRevenueCard);
        statsPanel.add(monthlyRevenueCard);
        
        JPanel pendingAmountCard = createStatsCard("⏰", "Montant en attente", "Chargement...", new Color(244, 67, 54));
        pendingAmountLabel = findValueLabel(pendingAmountCard);
        statsPanel.add(pendingAmountCard);
        
        dashboard.add(headerPanel, BorderLayout.NORTH);
        dashboard.add(statsPanel, BorderLayout.CENTER);
        
        // Welcome message
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JLabel welcomeLabel = new JLabel("Bienvenue dans votre système de gestion");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setForeground(new Color(108, 117, 125));
        
        JLabel instructionLabel = new JLabel("Utilisez les onglets ci-dessus pour gérer vos clients et versements");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructionLabel.setForeground(new Color(108, 117, 125));
        
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createVerticalStrut(10));
        welcomePanel.add(instructionLabel);
        
        dashboard.add(welcomePanel, BorderLayout.SOUTH);
        
        // Load initial data
        SwingUtilities.invokeLater(this::updateDashboardData);
        
        return dashboard;
    }

    private JPanel createStatsCard(String icon, String title, String value, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setBackground(Color.WHITE);
        
        // Icon and title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(108, 117, 125));
        
        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        
        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(accentColor);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JLabel findValueLabel(JPanel card) {
        // Find the value label in the card (it's the second component in the BorderLayout.CENTER)
        Component centerComponent = ((BorderLayout) card.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (centerComponent instanceof JLabel) {
            return (JLabel) centerComponent;
        }
        return null;
    }
    
    private void updateDashboardData() {
        // Update dashboard data in background thread
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private int totalClients = 0;
            private BigDecimal totalVersements = BigDecimal.ZERO;
            private BigDecimal monthlyRevenue = BigDecimal.ZERO;
            private BigDecimal pendingAmount = BigDecimal.ZERO;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Get total clients
                    List<Client> clients = clientController.fetchAllClients();
                    totalClients = clients.size();
                    
                    // Calculate total versements and pending amounts
                    List<Versment> allVersements = versmentController.fetchAllVersments();
                    
                    // Calculate total versements amount
                    for (Versment versment : allVersements) {
                        if (versment.getMontant() != null) {
                            totalVersements = totalVersements.add(versment.getMontant());
                        }
                    }
                    
                    // Calculate monthly revenue (current month)
                    LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
                    LocalDate nextMonth = currentMonth.plusMonths(1);
                    
                    for (Versment versment : allVersements) {
                        if (versment.getDatePaiement() != null && 
                            versment.getDatePaiement().isAfter(currentMonth.minusDays(1)) &&
                            versment.getDatePaiement().isBefore(nextMonth)) {
                            if (versment.getMontant() != null) {
                                monthlyRevenue = monthlyRevenue.add(versment.getMontant());
                            }
                        }
                    }
                    
                    // Calculate pending amounts (sum of all remaining balances)
                    for (Client client : clients) {
                        BigDecimal remaining = versmentController.getRemainingAmountForClient(client.getId());
                        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                            pendingAmount = pendingAmount.add(remaining);
                        }
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error updating dashboard data: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                // Update UI components on EDT
                if (totalClientsLabel != null) {
                    totalClientsLabel.setText(String.valueOf(totalClients));
                }
                if (totalVersmentsLabel != null) {
                    totalVersmentsLabel.setText(totalVersements.toString() + " DA");
                }
                if (monthlyRevenueLabel != null) {
                    monthlyRevenueLabel.setText(monthlyRevenue.toString() + " DA");
                }
                if (pendingAmountLabel != null) {
                    pendingAmountLabel.setText(pendingAmount.toString() + " DA");
                }
                
                updateStatus("✅ Tableau de bord mis à jour");
            }
        };
        
        worker.execute();
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // File menu
        JMenu fileMenu = new JMenu("📁 Fichier");
        fileMenu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JMenuItem newClientItem = new JMenuItem("👤 Nouveau Client");
        newClientItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newClientItem.addActionListener(e -> switchToClientsTab());
        
        JMenuItem newVersmentItem = new JMenuItem("💳 Nouveau Versement");
        newVersmentItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newVersmentItem.addActionListener(e -> switchToVersmentsTab());
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("🚪 Quitter");
        exitItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        exitItem.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir quitter l'application?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        
        fileMenu.add(newClientItem);
        fileMenu.add(newVersmentItem);
        fileMenu.add(exitItem);

        // View menu
        JMenu viewMenu = new JMenu("👁️ Affichage");
        viewMenu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        themeToggleButton = new JMenuItem("🌙 Mode Sombre");
        themeToggleButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        themeToggleButton.addActionListener(e -> toggleTheme());
        
        JMenuItem refreshItem = new JMenuItem("🔄 Actualiser");
        refreshItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        refreshItem.addActionListener(e -> refreshAllData());
        
        viewMenu.add(themeToggleButton);
        viewMenu.addSeparator();
        viewMenu.add(refreshItem);

        // Help menu
        JMenu helpMenu = new JMenu("❓ Aide");
        helpMenu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JMenuItem aboutItem = new JMenuItem("ℹ️ À propos");
        aboutItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        aboutItem.addActionListener(e -> showAboutDialog());
        
        JMenuItem helpItem = new JMenuItem("📖 Guide d'utilisation");
        helpItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        helpItem.addActionListener(e -> showHelpDialog());
        
        helpMenu.add(helpItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(Box.createHorizontalGlue()); // Push help menu to the right
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void setupStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        statusBar.setBackground(new Color(248, 249, 250));

        statusLabel = new JLabel("✅ Système prêt");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(108, 117, 125));

        JLabel versionLabel = new JLabel("Version 2.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(108, 117, 125));

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(versionLabel, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);
    }

    private void switchToClientsTab() {
        tabbedPane.setSelectedIndex(1); // Clients tab
        updateStatus("📋 Onglet Clients sélectionné");
    }

    private void switchToVersmentsTab() {
        tabbedPane.setSelectedIndex(2); // Versments tab
        updateStatus("💰 Onglet Versements sélectionné");
    }

    private void refreshAllData() {
        updateStatus("🔄 Actualisation des données...");
        // Here you could trigger refresh on all tabs
        SwingUtilities.invokeLater(() -> {
            updateStatus("✅ Données actualisées");
        });
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            
            // Auto-clear status after 3 seconds
            Timer timer = new Timer(3000, e -> statusLabel.setText("✅ Système prêt"));
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void toggleTheme() {
        try {
            if (isDarkMode) {
                UIManager.setLookAndFeel(new FlatLightLaf());
                themeToggleButton.setText("🌙 Mode Sombre");
                updateStatus("☀️ Thème clair activé");
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                themeToggleButton.setText("☀️ Mode Clair");
                updateStatus("🌙 Thème sombre activé");
            }
            SwingUtilities.updateComponentTreeUI(this);
            isDarkMode = !isDarkMode;
        } catch (Exception ex) {
            ex.printStackTrace();
            updateStatus("❌ Erreur lors du changement de thème");
        }
    }

    private void showWelcomeMessage() {
        // Create a subtle welcome notification
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        welcomePanel.setBackground(new Color(232, 245, 255));

        JLabel welcomeIcon = new JLabel("🎉");
        welcomeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        welcomeIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(new Color(232, 245, 255));

        JLabel titleLabel = new JLabel("Bienvenue dans votre système de gestion!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(52, 152, 219));

        JLabel subtitleLabel = new JLabel("Gérez facilement vos clients et leurs versements");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(108, 117, 125));

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitleLabel);

        JButton closeButton = new JButton("✕");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setForeground(new Color(108, 117, 125));
        closeButton.addActionListener(e -> {
            Container parent = welcomePanel.getParent();
            if (parent != null) {
                parent.remove(welcomePanel);
                parent.revalidate();
                parent.repaint();
            }
        });

        welcomePanel.add(welcomeIcon, BorderLayout.WEST);
        welcomePanel.add(textPanel, BorderLayout.CENTER);
        welcomePanel.add(closeButton, BorderLayout.EAST);

        // Add to the top of the main content
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.add(welcomePanel, BorderLayout.NORTH);
        contentWrapper.add(tabbedPane, BorderLayout.CENTER);
        
        remove(tabbedPane);
        add(contentWrapper, BorderLayout.CENTER);
        
        revalidate();
        repaint();

        // Auto-hide welcome message after 10 seconds
        Timer timer = new Timer(10000, e -> {
            Container parent = welcomePanel.getParent();
            if (parent != null) {
                parent.remove(welcomePanel);
                parent.revalidate();
                parent.repaint();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void showAboutDialog() {
        JDialog aboutDialog = new JDialog(this, "À propos", true);
        aboutDialog.setSize(400, 300);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        contentPanel.setBackground(Color.WHITE);

        // App icon
        JLabel iconLabel = new JLabel("💼");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // App name
        JLabel nameLabel = new JLabel("Système de Gestion");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setForeground(new Color(52, 152, 219));

        // Version
        JLabel versionLabel = new JLabel("Version 2.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionLabel.setForeground(new Color(108, 117, 125));

        // Description
        JLabel descLabel = new JLabel("<html><center>Application moderne de gestion<br>des clients et versements<br><br>Développé avec Java Swing & FlatLaf</center></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setForeground(new Color(108, 117, 125));

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(nameLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(versionLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(descLabel);

        JButton closeButton = new JButton("Fermer");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.addActionListener(e -> aboutDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);

        aboutDialog.add(contentPanel, BorderLayout.CENTER);
        aboutDialog.add(buttonPanel, BorderLayout.SOUTH);
        aboutDialog.setVisible(true);
    }

    private void showHelpDialog() {
        JDialog helpDialog = new JDialog(this, "Guide d'utilisation", true);
        helpDialog.setSize(500, 400);
        helpDialog.setLocationRelativeTo(this);
        helpDialog.setLayout(new BorderLayout());

        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        helpText.setMargin(new Insets(20, 20, 20, 20));
        helpText.setText(
            "📖 GUIDE D'UTILISATION\n\n" +
            "👥 GESTION DES CLIENTS:\n" +
            "• Ajouter un nouveau client avec ses informations\n" +
            "• Modifier les données d'un client existant\n" +
            "• Supprimer un client (supprime aussi ses versements)\n" +
            "• Rechercher et filtrer les clients\n\n" +
            "💰 GESTION DES VERSEMENTS:\n" +
            "• Enregistrer les paiements des clients\n" +
            "• Suivre les montants restants\n" +
            "• Modifier ou supprimer des versements\n" +
            "• Imprimer des reçus de versement\n\n" +
            "📊 TABLEAU DE BORD:\n" +
            "• Vue d'ensemble des statistiques\n" +
            "• Suivi des revenus et paiements\n\n" +
            "💡 CONSEILS:\n" +
            "• Utilisez la recherche pour trouver rapidement\n" +
            "• Clic droit sur les versements pour plus d'options\n" +
            "• Changez le thème avec le bouton dans le menu"
        );

        JScrollPane scrollPane = new JScrollPane(helpText);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JButton closeButton = new JButton("Fermer");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.addActionListener(e -> helpDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);

        helpDialog.add(scrollPane, BorderLayout.CENTER);
        helpDialog.add(buttonPanel, BorderLayout.SOUTH);
        helpDialog.setVisible(true);
    }

    public static void main(String[] args) {
        // Set system properties for better rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Erreur lors du démarrage de l'application:\n" + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}