package edu.inventory.administrator.movements.domain.service.repository;

import edu.inventory.administrator.datastructures.LinkedList;
import edu.inventory.administrator.movements.domain.entity.ProductMovement;
import edu.inventory.administrator.movements.domain.valueobject.MovementId;
import edu.inventory.administrator.products.domain.valueobject.ProductId;

public interface MovementRepository {
    void saveMovement(ProductMovement movement);
    ProductMovement getMovement(MovementId id);
    LinkedList<ProductMovement> getAllMovements();
    LinkedList<ProductMovement> getMovementsByProductId(ProductId productId);
}
