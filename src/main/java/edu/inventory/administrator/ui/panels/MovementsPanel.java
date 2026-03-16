package edu.inventory.administrator.ui.panels;

import edu.inventory.administrator.movements.domain.entity.ProductMovement;
import edu.inventory.administrator.movements.domain.service.InventoryDomainService;
import edu.inventory.administrator.movements.domain.valueobject.MovementType;
import edu.inventory.administrator.products.domain.entity.Product;
import edu.inventory.administrator.products.domain.valueobject.ProductId;
import edu.inventory.administrator.services.CompositionRoot;
import edu.inventory.administrator.ui.support.Formatters;
import edu.inventory.administrator.ui.support.UiMessages;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class MovementsPanel extends JPanel {
    private final InventoryDomainService inventoryService;
    private final JComboBox<String> productCombo;
    private final JComboBox<MovementType> typeCombo;
    private final JTextField quantityField;
    private final JTextArea reasonArea;
    private final DefaultTableModel movementsTableModel;
    private final DefaultTableModel productMovementsTableModel;
    private Runnable onDataChanged;

    public MovementsPanel(CompositionRoot root) {
        this.inventoryService = root.inventoryService();
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Registrar movimiento"));

        productCombo = new JComboBox<>();
        typeCombo = new JComboBox<>(MovementType.values());
        quantityField = new JTextField();
        reasonArea = new JTextArea(4, 20);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);

        form.add(new JLabel("Producto:"));
        form.add(productCombo);
        form.add(new JLabel("Tipo de operación:"));
        form.add(typeCombo);
        form.add(new JLabel("Cantidad afectada:"));
        form.add(quantityField);
        form.add(new JLabel("Motivo / comentario:"));
        form.add(new JScrollPane(reasonArea));

        JPanel buttons = new JPanel(new GridLayout(1, 4, 8, 8));
        JButton registerButton = new JButton("Registrar movimiento");
        JButton undoButton = new JButton("Deshacer último movimiento");
        JButton byProductButton = new JButton("Ver por producto");
        JButton reloadButton = new JButton("Recargar");
        buttons.add(registerButton);
        buttons.add(undoButton);
        buttons.add(byProductButton);
        buttons.add(reloadButton);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        movementsTableModel = createMovementTableModel();
        JTable movementsTable = new JTable(movementsTableModel);
        movementsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        productMovementsTableModel = createMovementTableModel();
        JTable productMovementsTable = new JTable(productMovementsTableModel);
        productMovementsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane allScroll = new JScrollPane(movementsTable);
        allScroll.setBorder(BorderFactory.createTitledBorder("Todos los movimientos"));

        JScrollPane productScroll = new JScrollPane(productMovementsTable);
        productScroll.setBorder(BorderFactory.createTitledBorder("Movimientos del producto seleccionado"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, allScroll, productScroll);
        splitPane.setResizeWeight(0.6);

        add(top, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        registerButton.addActionListener(event -> registerMovement());
        undoButton.addActionListener(event -> undoMovement());
        byProductButton.addActionListener(event -> refreshByProductTable());
        reloadButton.addActionListener(event -> refreshTables());

        refreshProducts();
        refreshTables();
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void refreshProducts() {
        String previousSelection = (String) productCombo.getSelectedItem();
        productCombo.removeAllItems();
        for (Product product : inventoryService.getAllProducts()) {
            productCombo.addItem(product.getId().getValue() + " - " + product.getName());
        }
        if (previousSelection != null) {
            productCombo.setSelectedItem(previousSelection);
        }
    }

    public void refreshTables() {
        fillMovementTable(movementsTableModel, inventoryService.getAllMovements());
        refreshByProductTable();
    }

    private void refreshByProductTable() {
        ProductId productId = selectedProductId();
        if (productId == null) {
            productMovementsTableModel.setRowCount(0);
            return;
        }
        fillMovementTable(productMovementsTableModel, inventoryService.getMovementsByProduct(productId));
    }

    private void registerMovement() {
        try {
            ProductId productId = requireSelectedProduct();
            MovementType type = (MovementType) typeCombo.getSelectedItem();
            int quantity = parseQuantity(quantityField.getText());
            String reason = reasonArea.getText();

            ProductMovement movement = inventoryService.registerMovement(productId, type, quantity, reason);
            refreshTables();
            clearForm();
            UiMessages.info(this, "Movimiento registrado: " + movement.getId().getValue());
            notifyDataChanged();
        } catch (RuntimeException exception) {
            UiMessages.error(this, exception.getMessage());
        }
    }

    private void undoMovement() {
        try {
            ProductId productId = requireSelectedProduct();
            ProductMovement movement = inventoryService.undoLastMovement(productId, normalizeReason(reasonArea.getText()));
            refreshTables();
            clearForm();
            UiMessages.info(this, "Se generó una corrección: " + movement.getId().getValue());
            notifyDataChanged();
        } catch (RuntimeException exception) {
            UiMessages.error(this, exception.getMessage());
        }
    }

    private DefaultTableModel createMovementTableModel() {
        return new DefaultTableModel(
                new Object[]{"ID", "Tipo", "Producto", "Cantidad", "Fecha/Hora", "Motivo", "Referencia previa"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void fillMovementTable(DefaultTableModel model, edu.inventory.administrator.datastructures.LinkedList<ProductMovement> movements) {
        model.setRowCount(0);
        for (ProductMovement movement : movements) {
            model.addRow(new Object[]{
                    movement.getId().getValue(),
                    movement.getType(),
                    movement.getProductId().getValue(),
                    movement.getQuantity(),
                    Formatters.dateTime(movement.getCreatedAt()),
                    Formatters.text(movement.getReason()),
                    movement.getPreviousOperationReference() == null ? "-" : movement.getPreviousOperationReference().getValue()
            });
        }
    }

    private ProductId requireSelectedProduct() {
        ProductId productId = selectedProductId();
        if (productId == null) {
            throw new IllegalArgumentException("Debes seleccionar un producto.");
        }
        return productId;
    }

    private ProductId selectedProductId() {
        String selection = (String) productCombo.getSelectedItem();
        if (selection == null || selection.isBlank()) {
            return null;
        }
        String id = selection.split(" - ", 2)[0].trim();
        return new ProductId(id);
    }

    private int parseQuantity(String value) {
        try {
            int quantity = Integer.parseInt(value.trim());
            if (quantity <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            }
            return quantity;
        } catch (Exception exception) {
            throw new IllegalArgumentException("La cantidad debe ser un entero válido.");
        }
    }

    private String normalizeReason(String value) {
        return value == null || value.isBlank() ? "Interfaz gráfica" : value.trim();
    }

    private void clearForm() {
        quantityField.setText("");
        reasonArea.setText("");
        typeCombo.setSelectedItem(MovementType.INCOME);
    }

    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }
}
