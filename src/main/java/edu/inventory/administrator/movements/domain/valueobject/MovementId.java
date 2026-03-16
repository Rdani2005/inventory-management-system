package edu.inventory.administrator.movements.domain.valueobject;

import edu.inventory.administrator.domain.valueobject.BaseId;

public class MovementId extends BaseId<String> {
    public MovementId(String value) {
        super(value);
    }
}
