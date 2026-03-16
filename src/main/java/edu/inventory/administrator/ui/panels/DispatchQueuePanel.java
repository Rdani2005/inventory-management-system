package edu.inventory.administrator.ui.panels;

import edu.inventory.administrator.movements.domain.entity.ProductMovement;
import edu.inventory.administrator.movements.domain.service.InventoryDomainService;
import edu.inventory.administrator.services.CompositionRoot;
import edu.inventory.administrator.ui.support.Formatters;
import edu.inventory.administrator.ui.support.UiMessages;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class DispatchQueuePanel extends JPanel {
    private final InventoryDomainService inventoryService;
    private final DefaultTableModel tableModel;
    private Runnable onDataChanged;

    public DispatchQueuePanel(CompositionRoot root) {
        this.inventoryService = root.inventoryService();
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel actions = new JPanel(new GridLayout(1, 2, 8, 8));
        JButton processNextButton = new JButton("Procesar siguiente despacho");
        JButton reloadButton = new JButton("Recargar cola");
        actions.add(processNextButton);
        actions.add(reloadButton);

        tableModel = new DefaultTableModel(
                new Object[]{"ID movimiento", "Producto", "Cantidad", "Fecha/Hora", "Motivo"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Despachos pendientes (Queue propia)"));

        add(actions, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        processNextButton.addActionListener(event -> processNextDispatch());
        reloadButton.addActionListener(event -> refreshTable());

        refreshTable();
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (ProductMovement movement : inventoryService.getPendingDispatches()) {
            tableModel.addRow(new Object[]{
                    movement.getId().getValue(),
                    movement.getProductId().getValue(),
                    movement.getQuantity(),
                    Formatters.dateTime(movement.getCreatedAt()),
                    Formatters.text(movement.getReason())
            });
        }
    }

    private void processNextDispatch() {
        try {
            ProductMovement movement = inventoryService.processNextDispatch();
            refreshTable();
            UiMessages.info(this, "Despacho procesado: " + movement.getId().getValue());
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        } catch (RuntimeException exception) {
            UiMessages.error(this, exception.getMessage());
        }
    }
}
