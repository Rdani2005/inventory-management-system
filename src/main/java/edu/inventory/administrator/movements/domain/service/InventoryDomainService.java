package edu.inventory.administrator.movements.domain.service;

import edu.inventory.administrator.datastructures.LinkedList;
import edu.inventory.administrator.movements.domain.entity.ProductMovement;
import edu.inventory.administrator.movements.domain.valueobject.MovementType;
import edu.inventory.administrator.products.domain.entity.Product;
import edu.inventory.administrator.products.domain.valueobject.ProductId;

public interface InventoryDomainService {
    ProductMovement registerMovement(ProductId productId, MovementType type, int quantity, String reason);
    ProductMovement undoLastMovement(ProductId productId, String reason);
    LinkedList<ProductMovement> getAllMovements();
    LinkedList<ProductMovement> getMovementsByProduct(ProductId productId);
    LinkedList<ProductMovement> getPendingDispatches();
    ProductMovement processNextDispatch();
    Product getProduct(ProductId productId);
    LinkedList<Product> getAllProducts();
}
