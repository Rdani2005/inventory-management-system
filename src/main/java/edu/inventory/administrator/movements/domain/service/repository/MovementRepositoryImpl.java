package edu.inventory.administrator.movements.domain.service.repository;

import edu.inventory.administrator.datastructures.LinkedList;
import edu.inventory.administrator.movements.domain.entity.ProductMovement;
import edu.inventory.administrator.movements.domain.valueobject.MovementId;
import edu.inventory.administrator.products.domain.valueobject.ProductId;
import java.util.Objects;

public class MovementRepositoryImpl implements MovementRepository {
    private final LinkedList<ProductMovement> movements;

    public MovementRepositoryImpl() {
        this.movements = new LinkedList<>();
    }

    @Override
    public void saveMovement(ProductMovement movement) {
        Objects.requireNonNull(movement, "movement must not be null");
        boolean updated = movements.update(current -> current.getId().equals(movement.getId()), movement);
        if (!updated) {
            movements.add(movement);
        }
    }

    @Override
    public ProductMovement getMovement(MovementId id) {
        if (id == null) {
            return null;
        }
        return movements.find(movement -> movement.getId().equals(id));
    }

    @Override
    public LinkedList<ProductMovement> getAllMovements() {
        return movements.copy();
    }

    @Override
    public LinkedList<ProductMovement> getMovementsByProductId(ProductId productId) {
        return movements.findAll(movement -> movement.getProductId().equals(productId));
    }
}
