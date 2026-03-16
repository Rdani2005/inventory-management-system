package edu.inventory.administrator.products.domain.exception;

import edu.inventory.administrator.domain.exception.DomainException;

public class ProductDomainException extends DomainException {
    public ProductDomainException(String message) {
        super(message);
    }
}
