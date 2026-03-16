package edu.inventory.administrator.services;

import edu.inventory.administrator.datastructures.Queue;
import edu.inventory.administrator.datastructures.Stack;
import edu.inventory.administrator.movements.domain.entity.ProductMovement;
import edu.inventory.administrator.movements.domain.service.InventoryDomainService;
import edu.inventory.administrator.movements.domain.service.InventoryDomainServiceImpl;
import edu.inventory.administrator.movements.domain.service.repository.MovementRepository;
import edu.inventory.administrator.movements.domain.service.repository.MovementRepositoryImpl;
import edu.inventory.administrator.products.domain.service.ProductDomainService;
import edu.inventory.administrator.products.domain.service.ProductDomainServiceImpl;
import edu.inventory.administrator.products.domain.service.repository.ProductRepository;
import edu.inventory.administrator.products.domain.service.repository.ProductRepositoryImpl;

public class CompositionRoot {
    private final ProductRepository productRepository;
    private final MovementRepository movementRepository;
    private final ProductDomainService productService;
    private final InventoryDomainService inventoryService;
    private final Stack<ProductMovement> movementHistory;
    private final Queue<ProductMovement> pendingDispatches;

    private static final class Holder {
        private static final CompositionRoot INSTANCE = new CompositionRoot();
    }

    public static CompositionRoot getInstance() {
        return Holder.INSTANCE;
    }

    private CompositionRoot() {
        this.productRepository = new ProductRepositoryImpl();
        this.movementRepository = new MovementRepositoryImpl();
        this.productService = new ProductDomainServiceImpl(productRepository);
        this.movementHistory = new Stack<>();
        this.pendingDispatches = new Queue<>();
        this.inventoryService = new InventoryDomainServiceImpl(
                productService,
                productRepository,
                movementRepository,
                movementHistory,
                pendingDispatches
        );
    }

    public ProductRepository productRepository() {
        return productRepository;
    }

    public MovementRepository movementRepository() {
        return movementRepository;
    }

    public ProductDomainService productService() {
        return productService;
    }

    public InventoryDomainService inventoryService() {
        return inventoryService;
    }
}
