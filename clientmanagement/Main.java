package com.yourcompany.clientmanagement;

import com.yourcompany.clientmanagement.view.MainFrame;
import com.yourcompany.clientmanagement.view.ModernLoginPage;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set system properties for better rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            try {
                ModernLoginPage loginPage = new ModernLoginPage();
                loginPage.setVisible(true);

                // Block until login window is closed
                loginPage.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        System.out.println("Login window closed. Login successful: " + loginPage.isLoginSuccessful());
                        if (loginPage.isLoginSuccessful()) {
                            try {
                                System.out.println("Creating main frame...");
                                MainFrame mainFrame = new MainFrame();
                                mainFrame.setVisible(true);
                                System.out.println("Main frame created and shown successfully.");
                            } catch (Exception ex) {
                                System.err.println("Error creating main frame: " + ex.getMessage());
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(null,
                                    "Erreur lors du chargement de l'application principale:\n" + ex.getMessage(),
                                    "Erreur",
                                    JOptionPane.ERROR_MESSAGE);
                                System.exit(1);
                            }
                        } else {
                            System.out.println("Login failed or cancelled. Exiting application.");
                            System.exit(0); // exit if login failed/closed
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Error starting application: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Erreur lors du démarrage de l'application:\n" + e.getMessage(),
                    "Erreur de démarrage",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
