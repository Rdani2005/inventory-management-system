package edu.inventory.administrator.application;

import edu.inventory.administrator.services.CompositionRoot;
import edu.inventory.administrator.ui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainApplication {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            CompositionRoot root = CompositionRoot.getInstance();
            MainFrame frame = new MainFrame(root);
            frame.setVisible(true);
        });
    }
}
