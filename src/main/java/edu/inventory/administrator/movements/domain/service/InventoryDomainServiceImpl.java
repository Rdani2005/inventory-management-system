package edu.inventory.administrator.movements.domain.service;

import edu.inventory.administrator.datastructures.LinkedList;
import edu.inventory.administrator.datastructures.Queue;
import edu.inventory.administrator.datastructures.Stack;
import edu.inventory.administrator.movements.domain.entity.ProductMovement;
import edu.inventory.administrator.movements.domain.exception.MovementDomainException;
import edu.inventory.administrator.movements.domain.exception.MovementNotFoundException;
import edu.inventory.administrator.movements.domain.service.repository.MovementRepository;
import edu.inventory.administrator.movements.domain.valueobject.MovementType;
import edu.inventory.administrator.products.domain.entity.Product;
import edu.inventory.administrator.products.domain.exception.ProductDomainException;
import edu.inventory.administrator.products.domain.service.ProductDomainService;
import edu.inventory.administrator.products.domain.service.repository.ProductRepository;
import edu.inventory.administrator.products.domain.valueobject.ProductId;
import edu.inventory.administrator.products.domain.valueobject.ProductStatus;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class InventoryDomainServiceImpl implements InventoryDomainService {
    private final ProductDomainService productService;
    private final ProductRepository productRepository;
    private final MovementRepository movementRepository;
    private final Stack<ProductMovement> history;
    private final Queue<ProductMovement> pendingDispatches;

    public InventoryDomainServiceImpl(
            ProductDomainService productService,
            ProductRepository productRepository,
            MovementRepository movementRepository,
            Stack<ProductMovement> history,
            Queue<ProductMovement> pendingDispatches
    ) {
        this.productService = Objects.requireNonNull(productService, "productService must not be null");
        this.productRepository = Objects.requireNonNull(productRepository, "productRepository must not be null");
        this.movementRepository = Objects.requireNonNull(movementRepository, "movementRepository must not be null");
        this.history = Objects.requireNonNull(history, "history must not be null");
        this.pendingDispatches = Objects.requireNonNull(pendingDispatches, "pendingDispatches must not be null");
    }

    @Override
    public ProductMovement registerMovement(ProductId productId, MovementType type, int quantity, String reason) {
        if (quantity <= 0) {
            throw new MovementDomainException("Quantity must be greater than zero.");
        }

        Product product = productService.getProduct(productId);
        ProductMovement lastMovement = findLastMovementForProduct(productId);

        applyMovementToProduct(product, type, quantity);
        productRepository.saveProduct(product);

        ProductMovement movement = new ProductMovement.Builder()
                .id(new edu.inventory.administrator.movements.domain.valueobject.MovementId(UUID.randomUUID().toString()))
                .type(type)
                .productId(productId)
                .quantity(quantity)
                .createdAt(LocalDateTime.now())
                .reason(normalize(reason))
                .previousOperationReference(lastMovement == null ? null : lastMovement.getId())
                .build();

        movementRepository.saveMovement(movement);
        history.push(movement);

        if (type == MovementType.DISPATCH) {
            pendingDispatches.enqueue(movement);
        }

        return movement;
    }

    @Override
    public ProductMovement undoLastMovement(ProductId productId, String reason) {
        ProductMovement target = extractLastMovementForProduct(productId);
        if (target == null) {
            throw new MovementNotFoundException("No movements found for the selected product.");
        }

        Product product = productService.getProduct(productId);
        reverseMovementOnProduct(product, target);
        productRepository.saveProduct(product);

        ProductMovement compensation = new ProductMovement.Builder()
                .id(new edu.inventory.administrator.movements.domain.valueobject.MovementId(UUID.randomUUID().toString()))
                .type(MovementType.CORRECTION)
                .productId(productId)
                .quantity(target.getQuantity())
                .createdAt(LocalDateTime.now())
                .reason(normalize(reason) + " | Undo of movement " + target.getId())
                .previousOperationReference(target.getId())
                .build();

        movementRepository.saveMovement(compensation);
        history.push(compensation);
        return compensation;
    }

    @Override
    public LinkedList<ProductMovement> getAllMovements() {
        return movementRepository.getAllMovements();
    }

    @Override
    public LinkedList<ProductMovement> getMovementsByProduct(ProductId productId) {
        return movementRepository.getMovementsByProductId(productId);
    }

    @Override
    public LinkedList<ProductMovement> getPendingDispatches() {
        return pendingDispatches.toLinkedList();
    }

    @Override
    public ProductMovement processNextDispatch() {
        ProductMovement movement = pendingDispatches.dequeue();
        if (movement == null) {
            throw new MovementNotFoundException("There are no pending dispatches.");
        }
        return movement;
    }

    @Override
    public Product getProduct(ProductId productId) {
        return productService.getProduct(productId);
    }

    @Override
    public LinkedList<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    private void applyMovementToProduct(Product product, MovementType type, int quantity) {
        switch (type) {
            case INCOME -> {
                product.addQuantity(quantity);
                product.setStatus(ProductStatus.AVAILABLE);
            }
            case DISPATCH -> {
                ensureEnoughStock(product, quantity);
                product.removeQuantity(quantity);
                product.setStatus(product.getQuantity() == 0 ? ProductStatus.DISPATCHED : ProductStatus.AVAILABLE);
            }
            case RETURN -> {
                product.addQuantity(quantity);
                product.setStatus(ProductStatus.RETURNED);
            }
            case CANCELLATION -> {
                ensureEnoughStock(product, quantity);
                product.removeQuantity(quantity);
                product.setStatus(ProductStatus.CANCELED);
            }
            case CORRECTION -> {
                if (quantity >= 0) {
                    product.addQuantity(quantity);
                } else {
                    ensureEnoughStock(product, Math.abs(quantity));
                    product.removeQuantity(Math.abs(quantity));
                }
                product.setStatus(ProductStatus.AVAILABLE);
            }
        }
    }

    private void reverseMovementOnProduct(Product product, ProductMovement movement) {
        switch (movement.getType()) {
            case INCOME, RETURN, CORRECTION -> {
                ensureEnoughStock(product, movement.getQuantity());
                product.removeQuantity(movement.getQuantity());
                product.setStatus(product.getQuantity() > 0 ? ProductStatus.AVAILABLE : ProductStatus.CANCELED);
            }
            case DISPATCH, CANCELLATION -> {
                product.addQuantity(movement.getQuantity());
                product.setStatus(ProductStatus.AVAILABLE);
            }
        }
    }

    private void ensureEnoughStock(Product product, int quantity) {
        if (product.getQuantity() < quantity) {
            throw new ProductDomainException("Not enough stock for this operation.");
        }
    }

    private ProductMovement findLastMovementForProduct(ProductId productId) {
        LinkedList<ProductMovement> movements = movementRepository.getMovementsByProductId(productId);
        return movements.isEmpty() ? null : movements.last();
    }

    private ProductMovement extractLastMovementForProduct(ProductId productId) {
        Stack<ProductMovement> buffer = new Stack<>();
        ProductMovement target = null;

        while (!history.isEmpty()) {
            ProductMovement current = history.pop();
            if (current.getProductId().equals(productId) && current.getType() != MovementType.CORRECTION) {
                target = current;
                break;
            }
            buffer.push(current);
        }

        while (!buffer.isEmpty()) {
            history.push(buffer.pop());
        }

        return target;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "N/A" : value.trim();
    }
}
