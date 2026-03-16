package edu.inventory.administrator.ui.support;

import java.awt.Component;
import javax.swing.JOptionPane;

public final class UiMessages {
    private UiMessages() {
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
