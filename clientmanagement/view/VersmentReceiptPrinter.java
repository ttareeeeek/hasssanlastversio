package com.yourcompany.clientmanagement.view;

import com.yourcompany.clientmanagement.model.Client;
import com.yourcompany.clientmanagement.model.Versment;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.time.format.DateTimeFormatter;

public class VersmentReceiptPrinter implements Printable {
    private Versment versment;
    private Client client;
    
    public VersmentReceiptPrinter(Versment versment, Client client) {
        this.versment = versment;
        this.client = client;
    }
    
    public void printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        
        // Show print dialog
        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(null, 
                    "Bon de versement envoyé à l'imprimante avec succès!", 
                    "Impression", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(null, 
                    "Erreur lors de l'impression: " + e.getMessage(), 
                    "Erreur d'impression", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void showPreview() {
        JDialog previewDialog = new JDialog((Frame) null, "Aperçu du bon de versement", true);
        previewDialog.setSize(600, 800);
        previewDialog.setLocationRelativeTo(null);
        
        JPanel previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Scale for preview
                double scale = 0.8;
                g2d.scale(scale, scale);
                
                // Draw the receipt
                drawReceipt(g2d, 0, 0, (int)(getWidth() / scale), (int)(getHeight() / scale));
                g2d.dispose();
            }
        };
        previewPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(previewPanel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton printButton = new JButton("🖨️ Imprimer");
        printButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        printButton.addActionListener(e -> {
            previewDialog.dispose();
            printReceipt();
        });
        
        JButton closeButton = new JButton("Fermer");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.addActionListener(e -> previewDialog.dispose());
        
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        previewDialog.add(scrollPane, BorderLayout.CENTER);
        previewDialog.add(buttonPanel, BorderLayout.SOUTH);
        previewDialog.setVisible(true);
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Translate to printable area
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        // Draw the receipt
        drawReceipt(g2d, 0, 0, (int) pageFormat.getImageableWidth(), (int) pageFormat.getImageableHeight());
        
        return PAGE_EXISTS;
    }
    
    private void drawReceipt(Graphics2D g2d, int x, int y, int width, int height) {
        int currentY = y + 50;
        int leftMargin = x + 50;
        int rightMargin = width - 50;
        
        // Header - Cabinet Information
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2d.setColor(new Color(52, 152, 219));
        
        String cabinetName = "Cabinet Larbi Hassane";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(cabinetName);
        g2d.drawString(cabinetName, leftMargin + (rightMargin - leftMargin - textWidth) / 2, currentY);
        currentY += 30;
        
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String subtitle = "Bureau de Comptabilité & Commissariat Aux Comptes";
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(subtitle);
        g2d.drawString(subtitle, leftMargin + (rightMargin - leftMargin - textWidth) / 2, currentY);
        currentY += 25;
        
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2d.setColor(Color.BLACK);
        String address = "186, Rue Si Lakhdar Lakhdaria";
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(address);
        g2d.drawString(address, leftMargin + (rightMargin - leftMargin - textWidth) / 2, currentY);
        currentY += 20;
        
        String phone = "Tél : 0551-053-121/026-704-409";
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(phone);
        g2d.drawString(phone, leftMargin + (rightMargin - leftMargin - textWidth) / 2, currentY);
        currentY += 40;
        
        // Separator line
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(52, 152, 219));
        g2d.drawLine(leftMargin, currentY, rightMargin, currentY);
        currentY += 30;
        
        // Receipt title
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g2d.setColor(new Color(52, 152, 219));
        String receiptTitle = "BON DE VERSEMENT";
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(receiptTitle);
        g2d.drawString(receiptTitle, leftMargin + (rightMargin - leftMargin - textWidth) / 2, currentY);
        currentY += 50;
        
        // Receipt details
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2d.setColor(Color.BLACK);
        
        // Date
        String currentDate = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        g2d.drawString("Date: " + currentDate, rightMargin - 150, currentY);
        currentY += 30;
        
        // Receipt number (using versment ID)
        g2d.drawString("N° Reçu: " + String.format("%06d", versment.getId()), rightMargin - 150, currentY);
        currentY += 40;
        
        // Client information box
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(new Color(220, 220, 220));
        int boxHeight = 120;
        g2d.drawRect(leftMargin, currentY, rightMargin - leftMargin, boxHeight);
        
        // Client details
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.drawString("INFORMATIONS CLIENT", leftMargin + 15, currentY + 25);
        
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        String clientName = "Nom: " + (client.getNom() != null ? client.getNom() : "") + 
                           (client.getPrenom() != null && !client.getPrenom().isEmpty() ? " " + client.getPrenom() : "");
        g2d.drawString(clientName, leftMargin + 15, currentY + 45);
        
        if (client.getCompany() != null && !client.getCompany().isEmpty()) {
            g2d.drawString("Entreprise: " + client.getCompany(), leftMargin + 15, currentY + 65);
        }
        
        if (client.getActivite() != null && !client.getActivite().isEmpty()) {
            g2d.drawString("Activité: " + client.getActivite(), leftMargin + 15, currentY + 85);
        }
        
        if (client.getPhone() != null && !client.getPhone().isEmpty()) {
            g2d.drawString("Téléphone: " + client.getPhone(), leftMargin + 15, currentY + 105);
        }
        
        currentY += boxHeight + 40;
        
        // Payment details box
        g2d.setColor(new Color(220, 220, 220));
        int paymentBoxHeight = 100;
        g2d.drawRect(leftMargin, currentY, rightMargin - leftMargin, paymentBoxHeight);
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.drawString("DÉTAILS DU VERSEMENT", leftMargin + 15, currentY + 25);
        
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2d.drawString("Type: " + versment.getType(), leftMargin + 15, currentY + 45);
        g2d.drawString("Date de paiement: " + versment.getDatePaiement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
                      leftMargin + 15, currentY + 65);
        g2d.drawString("Année concernée: " + versment.getAnneeConcernee(), leftMargin + 15, currentY + 85);
        
        currentY += paymentBoxHeight + 30;
        
        // Amount - highlighted
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 24));
        g2d.setColor(new Color(46, 125, 50));
        String amountText = "MONTANT: " + versment.getMontant() + " DA";
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(amountText);
        
        // Draw amount background
        g2d.setColor(new Color(232, 245, 233));
        g2d.fillRoundRect(leftMargin + (rightMargin - leftMargin - textWidth) / 2 - 20, 
                         currentY - 25, textWidth + 40, 40, 10, 10);
        
        g2d.setColor(new Color(46, 125, 50));
        g2d.drawString(amountText, leftMargin + (rightMargin - leftMargin - textWidth) / 2, currentY);
        currentY += 60;
        
        // Footer
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        g2d.setColor(new Color(108, 117, 125));
        String footer = "Merci pour votre confiance - Cabinet Larbi Hassane";
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(footer);
        g2d.drawString(footer, leftMargin + (rightMargin - leftMargin - textWidth) / 2, currentY + 40);
        
        // Signature area
        currentY += 80;
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2d.drawString("Signature du client:", leftMargin, currentY);
        g2d.drawString("Cachet et signature:", rightMargin - 150, currentY);
        
        // Signature lines
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawLine(leftMargin, currentY + 40, leftMargin + 150, currentY + 40);
        g2d.drawLine(rightMargin - 150, currentY + 40, rightMargin, currentY + 40);
    }
}