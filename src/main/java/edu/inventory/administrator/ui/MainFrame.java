package edu.inventory.administrator.ui;

import edu.inventory.administrator.services.CompositionRoot;
import edu.inventory.administrator.ui.panels.DispatchQueuePanel;
import edu.inventory.administrator.ui.panels.MovementsPanel;
import edu.inventory.administrator.ui.panels.ProductsPanel;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {
    public MainFrame(CompositionRoot root) {
        setTitle("Inventory Administrator");
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        ProductsPanel productsPanel = new ProductsPanel(root);
        MovementsPanel movementsPanel = new MovementsPanel(root);
        DispatchQueuePanel dispatchQueuePanel = new DispatchQueuePanel(root);

        productsPanel.setOnDataChanged(() -> {
            movementsPanel.refreshProducts();
            movementsPanel.refreshTables();
            dispatchQueuePanel.refreshTable();
        });

        movementsPanel.setOnDataChanged(() -> {
            productsPanel.refreshTable();
            dispatchQueuePanel.refreshTable();
        });

        dispatchQueuePanel.setOnDataChanged(() -> movementsPanel.refreshTables());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Productos", productsPanel);
        tabs.addTab("Movimientos", movementsPanel);
        tabs.addTab("Cola de despachos", dispatchQueuePanel);

        add(tabs, BorderLayout.CENTER);
    }
}
