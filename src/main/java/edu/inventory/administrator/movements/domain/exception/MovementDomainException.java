package edu.inventory.administrator.movements.domain.exception;

import edu.inventory.administrator.domain.exception.DomainException;

public class MovementDomainException extends DomainException {
    public MovementDomainException(String message) {
        super(message);
    }
}
