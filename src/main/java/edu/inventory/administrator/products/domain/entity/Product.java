package edu.inventory.administrator.products.domain.entity;

import edu.inventory.administrator.domain.entity.AggregateRoot;
import edu.inventory.administrator.products.domain.valueobject.ProductId;
import edu.inventory.administrator.products.domain.valueobject.ProductStatus;
import java.time.LocalDateTime;

public class Product extends AggregateRoot<ProductId> {
    private final String name;
    private final String category;
    private int quantity;
    private final LocalDateTime createdAt;
    private final String supplier;
    private final String location;
    private ProductStatus status;

    private Product(Builder builder) {
        super.setId(builder.id);
        this.name = builder.name;
        this.category = builder.category;
        this.quantity = builder.quantity;
        this.createdAt = builder.createdAt;
        this.supplier = builder.supplier;
        this.location = builder.location;
        this.status = builder.status;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getSupplier() {
        return supplier;
    }

    public String getLocation() {
        return location;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    public void removeQuantity(int amount) {
        this.quantity -= amount;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                ", createdAt=" + createdAt +
                ", supplier='" + supplier + '\'' +
                ", location='" + location + '\'' +
                ", status=" + status +
                '}';
    }

    public static class Builder {
        private ProductId id;
        private String name;
        private String category;
        private int quantity;
        private LocalDateTime createdAt;
        private String supplier;
        private String location;
        private ProductStatus status;

        public Builder id(ProductId id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder supplier(String supplier) {
            this.supplier = supplier;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder status(ProductStatus status) {
            this.status = status;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}
