package edu.inventory.administrator.products.domain.valueobject;

import edu.inventory.administrator.domain.valueobject.BaseId;

public class ProductId extends BaseId<String> {
    public ProductId(String value) {
        super(value);
    }
}
