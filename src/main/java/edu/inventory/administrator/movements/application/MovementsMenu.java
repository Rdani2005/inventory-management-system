package edu.inventory.administrator.movements.application;

import edu.inventory.administrator.movements.domain.entity.ProductMovement;
import edu.inventory.administrator.movements.domain.service.InventoryDomainService;
import edu.inventory.administrator.movements.domain.valueobject.MovementType;
import edu.inventory.administrator.products.domain.valueobject.ProductId;
import edu.inventory.administrator.utilities.Console;

public class MovementsMenu {
    private final Console io;
    private final InventoryDomainService inventoryService;

    public MovementsMenu(Console io, InventoryDomainService inventoryService) {
        this.io = io;
        this.inventoryService = inventoryService;
    }

    public void run() {
        while (true) {
            io.header("Movements Menu");
            System.out.println("1) List all movements");
            System.out.println("2) List movements by product");
            System.out.println("3) Register income");
            System.out.println("4) Register dispatch");
            System.out.println("5) Register return");
            System.out.println("6) Register cancellation");
            System.out.println("7) Register correction");
            System.out.println("8) Undo last movement of a product");
            System.out.println("9) Show pending dispatch queue");
            System.out.println("10) Process next dispatch in queue");
            System.out.println("0) Back");

            int opt = io.promptInt("Choose: ");
            switch (opt) {
                case 1 -> listAll();
                case 2 -> listByProduct();
                case 3 -> register(MovementType.INCOME);
                case 4 -> register(MovementType.DISPATCH);
                case 5 -> register(MovementType.RETURN);
                case 6 -> register(MovementType.CANCELLATION);
                case 7 -> register(MovementType.CORRECTION);
                case 8 -> undo();
                case 9 -> showPendingDispatches();
                case 10 -> processNextDispatch();
                case 0 -> { return; }
                default -> io.err("Invalid option.");
            }
        }
    }

    private void listAll() {
        printMovements(inventoryService.getAllMovements());
    }

    private void listByProduct() {
        String productId = io.prompt("Product id: ");
        printMovements(inventoryService.getMovementsByProduct(new ProductId(productId)));
    }

    private void register(MovementType type) {
        String productId = io.prompt("Product id: ");
        int quantity = io.promptInt("Affected quantity: ");
        String reason = io.prompt("Reason / comment (optional): ");

        ProductMovement movement = inventoryService.registerMovement(new ProductId(productId), type, quantity, reason);
        io.ok("Movement registered: " + movement);
    }

    private void undo() {
        String productId = io.prompt("Product id: ");
        String reason = io.prompt("Reason / comment for undo: ");
        ProductMovement movement = inventoryService.undoLastMovement(new ProductId(productId), reason);
        io.ok("Undo generated as correction: " + movement);
    }

    private void showPendingDispatches() {
        printMovements(inventoryService.getPendingDispatches());
    }

    private void processNextDispatch() {
        ProductMovement movement = inventoryService.processNextDispatch();
        io.ok("Dispatch processed from queue: " + movement);
    }

    private void printMovements(edu.inventory.administrator.datastructures.LinkedList<ProductMovement> movements) {
        if (movements.isEmpty()) {
            io.info("(no movements)");
            return;
        }

        int index = 0;
        for (ProductMovement movement : movements) {
            System.out.printf("[%d] %s%n", index++, movement);
        }
    }
}
