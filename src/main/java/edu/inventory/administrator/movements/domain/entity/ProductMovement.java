package edu.inventory.administrator.movements.domain.entity;

import edu.inventory.administrator.domain.entity.AggregateRoot;
import edu.inventory.administrator.movements.domain.valueobject.MovementId;
import edu.inventory.administrator.movements.domain.valueobject.MovementType;
import edu.inventory.administrator.products.domain.valueobject.ProductId;
import java.time.LocalDateTime;

public class ProductMovement extends AggregateRoot<MovementId> {
    private final MovementType type;
    private final ProductId productId;
    private final int quantity;
    private final LocalDateTime createdAt;
    private final String reason;
    private final MovementId previousOperationReference;

    private ProductMovement(Builder builder) {
        super.setId(builder.id);
        this.type = builder.type;
        this.productId = builder.productId;
        this.quantity = builder.quantity;
        this.createdAt = builder.createdAt;
        this.reason = builder.reason;
        this.previousOperationReference = builder.previousOperationReference;
    }

    public MovementType getType() {
        return type;
    }

    public ProductId getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getReason() {
        return reason;
    }

    public MovementId getPreviousOperationReference() {
        return previousOperationReference;
    }

    @Override
    public String toString() {
        return "ProductMovement{" +
                "id=" + getId() +
                ", type=" + type +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", createdAt=" + createdAt +
                ", reason='" + reason + '\'' +
                ", previousOperationReference=" + previousOperationReference +
                '}';
    }

    public static class Builder {
        private MovementId id;
        private MovementType type;
        private ProductId productId;
        private int quantity;
        private LocalDateTime createdAt;
        private String reason;
        private MovementId previousOperationReference;

        public Builder id(MovementId id) {
            this.id = id;
            return this;
        }

        public Builder type(MovementType type) {
            this.type = type;
            return this;
        }

        public Builder productId(ProductId productId) {
            this.productId = productId;
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

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder previousOperationReference(MovementId previousOperationReference) {
            this.previousOperationReference = previousOperationReference;
            return this;
        }

        public ProductMovement build() {
            return new ProductMovement(this);
        }
    }
}
