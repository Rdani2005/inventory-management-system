package edu.inventory.administrator.products.domain.exception;

public class ProductNotFoundException extends ProductDomainException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
