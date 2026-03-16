package edu.inventory.administrator.movements.domain.exception;

public class MovementNotFoundException extends MovementDomainException {
    public MovementNotFoundException(String message) {
        super(message);
    }
}
