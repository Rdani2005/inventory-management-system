package edu.inventory.administrator.ui.panels;

import edu.inventory.administrator.products.domain.entity.Product;
import edu.inventory.administrator.products.domain.service.ProductDomainService;
import edu.inventory.administrator.products.domain.valueobject.ProductId;
import edu.inventory.administrator.products.domain.valueobject.ProductStatus;
import edu.inventory.administrator.services.CompositionRoot;
import edu.inventory.administrator.ui.support.Formatters;
import edu.inventory.administrator.ui.support.UiMessages;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class ProductsPanel extends JPanel {
    private final ProductDomainService productService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField categoryField;
    private final JTextField quantityField;
    private final JTextField supplierField;
    private final JTextField locationField;
    private final JComboBox<ProductStatus> statusCombo;
    private Runnable onDataChanged;

    public ProductsPanel(CompositionRoot root) {
        this.productService = root.productService();
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Registro de producto"));

        idField = new JTextField();
        nameField = new JTextField();
        categoryField = new JTextField();
        quantityField = new JTextField();
        supplierField = new JTextField();
        locationField = new JTextField();
        statusCombo = new JComboBox<>(ProductStatus.values());

        form.add(new JLabel("Código / ID:"));
        form.add(idField);
        form.add(new JLabel("Nombre / descripción:"));
        form.add(nameField);
        form.add(new JLabel("Categoría:"));
        form.add(categoryField);
        form.add(new JLabel("Cantidad:"));
        form.add(quantityField);
        form.add(new JLabel("Proveedor:"));
        form.add(supplierField);
        form.add(new JLabel("Ubicación / sección:"));
        form.add(locationField);
        form.add(new JLabel("Estado:"));
        form.add(statusCombo);

        JPanel buttons = new JPanel(new GridLayout(1, 4, 8, 8));
        JButton createButton = new JButton("Guardar producto");
        JButton deleteButton = new JButton("Eliminar por ID");
        JButton clearButton = new JButton("Limpiar");
        JButton reloadButton = new JButton("Recargar tabla");
        buttons.add(createButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);
        buttons.add(reloadButton);

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.add(form, BorderLayout.CENTER);
        left.add(buttons, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Categoría", "Cantidad", "Ingreso", "Proveedor", "Ubicación", "Estado"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Productos registrados"));

        add(left, BorderLayout.WEST);
        add(tableScroll, BorderLayout.CENTER);

        createButton.addActionListener(event -> saveProduct());
        deleteButton.addActionListener(event -> deleteProduct());
        clearButton.addActionListener(event -> clearFields());
        reloadButton.addActionListener(event -> refreshTable());
        table.getSelectionModel().addListSelectionListener(event -> fillFormFromSelection());

        refreshTable();
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (Product product : productService.getAllProducts()) {
            tableModel.addRow(new Object[]{
                    product.getId().getValue(),
                    product.getName(),
                    Formatters.text(product.getCategory()),
                    product.getQuantity(),
                    Formatters.dateTime(product.getCreatedAt()),
                    Formatters.text(product.getSupplier()),
                    Formatters.text(product.getLocation()),
                    product.getStatus()
            });
        }
    }

    private void saveProduct() {
        try {
            Product product = new Product.Builder()
                    .id(new ProductId(required(idField.getText(), "El ID es requerido.")))
                    .name(required(nameField.getText(), "El nombre es requerido."))
                    .category(normalize(categoryField.getText()))
                    .quantity(parseQuantity(quantityField.getText()))
                    .createdAt(LocalDateTime.now())
                    .supplier(normalize(supplierField.getText()))
                    .location(normalize(locationField.getText()))
                    .status((ProductStatus) statusCombo.getSelectedItem())
                    .build();

            productService.createProduct(product);
            refreshTable();
            clearFields();
            UiMessages.info(this, "Producto guardado correctamente.");
            notifyDataChanged();
        } catch (RuntimeException exception) {
            UiMessages.error(this, exception.getMessage());
        }
    }

    private void deleteProduct() {
        try {
            String id = required(idField.getText(), "Ingresa un ID para eliminar.");
            productService.deleteProduct(new ProductId(id));
            refreshTable();
            clearFields();
            UiMessages.info(this, "Producto eliminado correctamente.");
            notifyDataChanged();
        } catch (RuntimeException exception) {
            UiMessages.error(this, exception.getMessage());
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        categoryField.setText(normalizedFromTable(row, 2));
        quantityField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        supplierField.setText(normalizedFromTable(row, 5));
        locationField.setText(normalizedFromTable(row, 6));
        statusCombo.setSelectedItem(ProductStatus.valueOf(String.valueOf(tableModel.getValueAt(row, 7))));
    }

    private String normalizedFromTable(int row, int column) {
        String value = String.valueOf(tableModel.getValueAt(row, column));
        return "-".equals(value) ? "" : value;
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        categoryField.setText("");
        quantityField.setText("");
        supplierField.setText("");
        locationField.setText("");
        statusCombo.setSelectedItem(ProductStatus.AVAILABLE);
        table.clearSelection();
    }

    private int parseQuantity(String value) {
        try {
            int quantity = Integer.parseInt(required(value, "La cantidad es requerida."));
            if (quantity < 0) {
                throw new IllegalArgumentException("La cantidad no puede ser negativa.");
            }
            return quantity;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("La cantidad debe ser un número entero válido.");
        }
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }
}
