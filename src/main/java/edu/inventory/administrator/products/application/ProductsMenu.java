package edu.inventory.administrator.products.application;

import edu.inventory.administrator.products.domain.entity.Product;
import edu.inventory.administrator.products.domain.service.ProductDomainService;
import edu.inventory.administrator.products.domain.service.repository.ProductRepository;
import edu.inventory.administrator.products.domain.valueobject.ProductId;
import edu.inventory.administrator.products.domain.valueobject.ProductStatus;
import edu.inventory.administrator.utilities.Console;
import java.time.LocalDateTime;

public class ProductsMenu {
    private final Console io;
    private final ProductDomainService productService;
    private final ProductRepository productRepository;

    public ProductsMenu(Console io, ProductDomainService productService, ProductRepository productRepository) {
        this.io = io;
        this.productService = productService;
        this.productRepository = productRepository;
    }

    public void run() {
        while (true) {
            io.header("Products Menu");
            System.out.println("1) List products");
            System.out.println("2) Create product");
            System.out.println("3) Search product by id");
            System.out.println("4) Delete product");
            System.out.println("0) Back");

            int opt = io.promptInt("Choose: ");
            switch (opt) {
                case 1 -> list();
                case 2 -> create();
                case 3 -> search();
                case 4 -> delete();
                case 0 -> { return; }
                default -> io.err("Invalid option.");
            }
        }
    }

    private void list() {
        edu.inventory.administrator.datastructures.LinkedList<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            io.info("(no products)");
            return;
        }

        int index = 0;
        for (Product product : products) {
            System.out.printf("[%d] %s%n", index++, product);
        }
    }

    private void create() {
        String id = io.prompt("Product code / id: ");
        String name = io.prompt("Name / description: ");
        String category = io.prompt("Category (optional): ");
        int quantity = io.promptInt("Initial quantity: ");
        String supplier = io.prompt("Supplier (optional): ");
        String location = io.prompt("Location / section (optional): ");

        Product product = new Product.Builder()
                .id(new ProductId(id))
                .name(name)
                .category(emptyToNA(category))
                .quantity(quantity)
                .createdAt(LocalDateTime.now())
                .supplier(emptyToNA(supplier))
                .location(emptyToNA(location))
                .status(ProductStatus.AVAILABLE)
                .build();

        productService.createProduct(product);
        productRepository.saveProduct(product);
        io.ok("Product created successfully.");
    }

    private void search() {
        String id = io.prompt("Product id: ");
        Product product = productService.getProduct(new ProductId(id));
        io.info(product.toString());
    }

    private void delete() {
        String id = io.prompt("Product id to delete: ");
        productService.deleteProduct(new ProductId(id));
        io.ok("Product deleted.");
    }

    private String emptyToNA(String value) {
        return value == null || value.isBlank() ? "N/A" : value.trim();
    }
}
