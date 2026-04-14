# Inventory Administrator — Developer Manual

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack](#2-tech-stack)
3. [Project Structure](#3-project-structure)
4. [Architecture](#4-architecture)
5. [Custom Data Structures](#5-custom-data-structures)
6. [Domain Layer](#6-domain-layer)
7. [Products Module](#7-products-module)
8. [Movements Module](#8-movements-module)
9. [Dependency Injection — CompositionRoot](#9-dependency-injection--compositionroot)
10. [UI Layer](#10-ui-layer)
11. [Entry Point](#11-entry-point)
12. [Building and Running](#12-building-and-running)
13. [Business Logic Reference](#13-business-logic-reference)
14. [Data Persistence Model](#14-data-persistence-model)
15. [Error Handling](#15-error-handling)
16. [Known Limitations and Future Improvements](#16-known-limitations-and-future-improvements)

---

## 1. Project Overview

**Inventory Administrator** is a Java 17 desktop application for managing product inventory. It provides a Swing-based GUI where users can register products, record stock movements (income, dispatch, return, cancellation, correction), undo the last movement for any product, and process a FIFO dispatch queue.

The project serves as a demonstration of:
- Domain-Driven Design (DDD) principles in a Java application
- Custom generic data structures (LinkedList, Stack, Queue) built from scratch without `java.util`
- Composition Root pattern for dependency injection
- Swing UI wired cleanly to a domain layer

All data is held in memory; there is no database or file persistence.

---

## 2. Tech Stack

| Concern          | Technology                              |
|------------------|-----------------------------------------|
| Language         | Java 17                                 |
| Build tool       | Maven 3.x (`pom.xml`)                   |
| GUI              | Java Swing                              |
| Data persistence | In-memory (custom LinkedList)           |
| Dependencies     | None (zero third-party libraries)       |

Maven coordinates: `edu.inventory:inventory-management-project:1.0-SNAPSHOT`

---

## 3. Project Structure

```
InventoryManagementProject/
├── pom.xml
├── README.md
├── DEVELOPER_MANUAL.md
├── sources.txt                          # Flat list of all .java files
├── out/                                 # javac output (legacy)
├── target/                              # Maven build output
└── src/main/java/edu/inventory/administrator/
    ├── application/
    │   └── MainApplication.java         # Entry point
    ├── datastructures/
    │   ├── Node.java
    │   ├── LinkedList.java
    │   ├── Stack.java
    │   └── Queue.java
    ├── domain/
    │   ├── entity/
    │   │   ├── AggregateRoot.java
    │   │   └── BaseEntity.java
    │   ├── exception/
    │   │   └── DomainException.java
    │   └── valueobject/
    │       └── BaseId.java
    ├── products/
    │   ├── application/
    │   │   └── ProductsMenu.java        # Legacy console UI (unused)
    │   └── domain/
    │       ├── entity/
    │       │   └── Product.java
    │       ├── exception/
    │       │   ├── ProductDomainException.java
    │       │   └── ProductNotFoundException.java
    │       ├── service/
    │       │   ├── ProductDomainService.java
    │       │   ├── ProductDomainServiceImpl.java
    │       │   └── repository/
    │       │       ├── ProductRepository.java
    │       │       └── ProductRepositoryImpl.java
    │       └── valueobject/
    │           ├── ProductId.java
    │           └── ProductStatus.java
    ├── movements/
    │   ├── application/
    │   │   └── MovementsMenu.java       # Legacy console UI (unused)
    │   └── domain/
    │       ├── entity/
    │       │   └── ProductMovement.java
    │       ├── exception/
    │       │   ├── MovementDomainException.java
    │       │   └── MovementNotFoundException.java
    │       ├── service/
    │       │   ├── InventoryDomainService.java
    │       │   ├── InventoryDomainServiceImpl.java
    │       │   └── repository/
    │       │       ├── MovementRepository.java
    │       │       └── MovementRepositoryImpl.java
    │       └── valueobject/
    │           ├── MovementId.java
    │           └── MovementType.java
    ├── services/
    │   └── CompositionRoot.java         # DI container / singleton
    ├── ui/
    │   ├── MainFrame.java
    │   ├── panels/
    │   │   ├── DispatchQueuePanel.java
    │   │   ├── MovementsPanel.java
    │   │   └── ProductsPanel.java
    │   └── support/
    │       ├── Formatters.java
    │       └── UiMessages.java
    └── utilities/
        └── Console.java                 # Legacy console helper (unused in GUI)
```

---

## 4. Architecture

### 4.1 Layered Overview

The application follows a DDD-inspired layered architecture:

```
┌──────────────────────────────────────────────┐
│                  UI Layer                    │
│  MainFrame → ProductsPanel                   │
│           → MovementsPanel                   │
│           → DispatchQueuePanel               │
├──────────────────────────────────────────────┤
│              Application Layer               │
│  MainApplication (bootstrap)                 │
│  CompositionRoot (wires all dependencies)    │
├──────────────────────────────────────────────┤
│               Domain Layer                   │
│  ProductDomainService                        │
│  InventoryDomainService                      │
│  Product, ProductMovement (aggregates)       │
│  Value Objects, Exceptions                   │
├──────────────────────────────────────────────┤
│           Infrastructure Layer               │
│  ProductRepositoryImpl  (LinkedList-backed)  │
│  MovementRepositoryImpl (LinkedList-backed)  │
│  Stack<ProductMovement> (history)            │
│  Queue<ProductMovement> (dispatch queue)     │
└──────────────────────────────────────────────┘
```

### 4.2 Dependency Graph

```
MainApplication
    └─ CompositionRoot (singleton)
           ├─ ProductRepositoryImpl
           ├─ ProductDomainServiceImpl ─── ProductRepositoryImpl
           ├─ MovementRepositoryImpl
           ├─ InventoryDomainServiceImpl ── MovementRepositoryImpl
           │                             ── ProductRepositoryImpl
           │                             ── Stack<ProductMovement>
           │                             ── Queue<ProductMovement>
           └─ MainFrame
                  ├─ ProductsPanel    ── ProductDomainService
                  ├─ MovementsPanel   ── InventoryDomainService
                  │                   ── ProductDomainService
                  └─ DispatchQueuePanel ── InventoryDomainService
```

### 4.3 Cross-Panel Refresh Callbacks

Panels do not share mutable state directly. Instead, `MainFrame` passes `Runnable` callbacks so that a change in one panel can trigger a data refresh in another:

| Panel that changes data | Panels that get refreshed              |
|-------------------------|----------------------------------------|
| ProductsPanel           | MovementsPanel, DispatchQueuePanel     |
| MovementsPanel          | ProductsPanel, DispatchQueuePanel      |
| DispatchQueuePanel      | MovementsPanel                         |

---

## 5. Custom Data Structures

All structures are generic and live in `edu.inventory.administrator.datastructures`.  
They implement `Iterable<T>` so they work in enhanced for-loops.  
**None** of them rely on `java.util.Collection` or any third-party library.

### 5.1 `Node<T>`

A singly-linked node:

```java
class Node<T> {
    T value;
    Node<T> next;
}
```

### 5.2 `LinkedList<T>`

A doubly-tracked (head + tail) singly-linked list. Key API:

| Method | Description |
|--------|-------------|
| `add(T value)` | Append to tail |
| `get(int index)` | O(n) index access |
| `first()` / `last()` | Head / tail value |
| `find(Predicate<T>)` | First match or null |
| `findAll(Predicate<T>)` | All matches as new LinkedList |
| `update(Predicate<T>, T replacement)` | Replace first match |
| `remove(Predicate<T>)` | Remove first match |
| `copy()` | Shallow copy |
| `size()` / `isEmpty()` | Size utilities |

Used by both `ProductRepositoryImpl` and `MovementRepositoryImpl` as the backing store.

### 5.3 `Stack<T>`

LIFO structure backed by a `Node<T>` chain (no LinkedList):

| Method | Description |
|--------|-------------|
| `push(T value)` | Add to top |
| `pop()` | Remove and return top |
| `peek()` | Return top without removal |
| `isEmpty()` | True if empty |

Used in `InventoryDomainServiceImpl` to maintain movement history for undo operations.

### 5.4 `Queue<T>`

FIFO structure backed by head/tail `Node<T>` pointers:

| Method | Description |
|--------|-------------|
| `enqueue(T value)` | Add to tail |
| `dequeue()` | Remove and return head |
| `peek()` | Return head without removal |
| `isEmpty()` | True if empty |
| `toLinkedList()` | Convert to LinkedList for display |

Used in `InventoryDomainServiceImpl` to maintain the pending dispatch queue.

---

## 6. Domain Layer

Shared base types in `edu.inventory.administrator.domain`.

### 6.1 `BaseEntity<ID>`

Abstract entity. Equality and hash code are determined solely by the `id` field.

```java
public abstract class BaseEntity<ID> {
    protected final ID id;
}
```

### 6.2 `AggregateRoot<ID>`

Extends `BaseEntity<ID>`. Currently a marker class; intended for future aggregate-level behaviors (domain events, etc.).

### 6.3 `BaseId<T>`

Abstract, immutable value object for all identity types:

```java
public abstract class BaseId<T> {
    private final T value;
    // equals / hashCode based on value
}
```

Concrete subclasses: `ProductId` (wraps `String`), `MovementId` (wraps `String`).

### 6.4 `DomainException`

Base unchecked exception for all domain-level errors:

```java
public class DomainException extends RuntimeException { ... }
```

All module-specific exceptions extend this class.

---

## 7. Products Module

Package root: `edu.inventory.administrator.products`

### 7.1 `Product` (Aggregate Root)

Immutable fields set at construction time:

| Field | Type | Description |
|-------|------|-------------|
| `id` | `ProductId` | Unique string identifier |
| `name` | `String` | Product name |
| `category` | `String` | Product category |
| `createdAt` | `LocalDateTime` | Creation timestamp |
| `supplier` | `String` | Supplier name |
| `location` | `String` | Storage location |

Mutable fields (changed by inventory operations):

| Field | Type | Description |
|-------|------|-------------|
| `quantity` | `int` | Current stock count |
| `status` | `ProductStatus` | AVAILABLE / DISPATCHED / RETURNED / CANCELED |

**Builder pattern** — construction goes through `Product.Builder`:
```java
Product p = new Product.Builder()
    .id(new ProductId("P001"))
    .name("Widget")
    .category("Hardware")
    .quantity(100)
    .supplier("ACME")
    .location("Shelf A3")
    .build();
```

Mutation methods: `addQuantity(int)`, `removeQuantity(int)`, `setQuantity(int)`, `setStatus(ProductStatus)`.

### 7.2 `ProductStatus`

```java
enum ProductStatus { AVAILABLE, DISPATCHED, RETURNED, CANCELED }
```

### 7.3 `ProductRepository` / `ProductRepositoryImpl`

Interface:
```java
void saveProduct(Product product);
Product getProduct(ProductId id);           // throws ProductNotFoundException
LinkedList<Product> getAllProducts();
void deleteProduct(ProductId id);
```

`ProductRepositoryImpl` backs all four methods with a `LinkedList<Product>` stored in memory.

- `saveProduct` — adds if new, updates in-place if ID already exists.
- `getProduct` — linear search by ID; throws `ProductNotFoundException` if missing.
- `deleteProduct` — linear search + remove.

### 7.4 `ProductDomainService` / `ProductDomainServiceImpl`

Interface:
```java
void createProduct(Product product);            // throws if ID already exists
Product getProduct(ProductId id);
LinkedList<Product> getAllProducts();
void deleteProduct(ProductId id);
```

`ProductDomainServiceImpl` adds uniqueness validation on top of the repository:  
`createProduct` checks for an existing product with the same ID before delegating to `saveProduct`.

### 7.5 Exceptions

| Class | Extends | When thrown |
|-------|---------|-------------|
| `ProductDomainException` | `DomainException` | Generic product rule violation |
| `ProductNotFoundException` | `ProductDomainException` | Requested product ID not found |

---

## 8. Movements Module

Package root: `edu.inventory.administrator.movements`

### 8.1 `ProductMovement` (Aggregate Root)

All fields are immutable (set at construction):

| Field | Type | Description |
|-------|------|-------------|
| `id` | `MovementId` | Unique string identifier |
| `type` | `MovementType` | Movement category |
| `productId` | `ProductId` | Affected product |
| `quantity` | `int` | Units affected |
| `createdAt` | `LocalDateTime` | Movement timestamp |
| `reason` | `String` | Free-text justification |
| `previousOperationReference` | `MovementId` | ID of prior movement for this product (audit chain) |

**Builder pattern** mirrors `Product`:
```java
ProductMovement m = new ProductMovement.Builder()
    .id(new MovementId("M001"))
    .type(MovementType.INCOME)
    .productId(new ProductId("P001"))
    .quantity(50)
    .reason("Initial stock")
    .build();
```

### 8.2 `MovementType`

```java
enum MovementType { INCOME, DISPATCH, RETURN, CANCELLATION, CORRECTION }
```

| Value | Meaning |
|-------|---------|
| `INCOME` | Stock arrives; quantity increases |
| `DISPATCH` | Stock leaves; quantity decreases |
| `RETURN` | Dispatched goods come back; quantity increases |
| `CANCELLATION` | Stock removed (damaged/expired); quantity decreases |
| `CORRECTION` | System-generated reversal created by undo |

### 8.3 `MovementRepository` / `MovementRepositoryImpl`

Interface:
```java
void saveMovement(ProductMovement movement);
ProductMovement getMovement(MovementId id);                     // throws MovementNotFoundException
LinkedList<ProductMovement> getAllMovements();
LinkedList<ProductMovement> getMovementsByProductId(ProductId productId);
```

Backed by `LinkedList<ProductMovement>` in memory.

### 8.4 `InventoryDomainService` / `InventoryDomainServiceImpl`

This is the core orchestrator of the application. It holds:
- A reference to `MovementRepository`
- A reference to `ProductRepository`
- A `Stack<ProductMovement>` for undo history
- A `Queue<ProductMovement>` for pending dispatch queue

**Interface:**
```java
void registerMovement(ProductMovement movement);
void undoLastMovement(ProductId productId);
LinkedList<ProductMovement> getAllMovements();
LinkedList<ProductMovement> getMovementsByProductId(ProductId productId);
LinkedList<ProductMovement> getPendingDispatches();
void processNextDispatch();
```

**`registerMovement` flow:**
1. Validate `quantity > 0`.
2. Fetch the target `Product` from `ProductRepository`.
3. Call `applyMovementToProduct(product, movement)` to update quantity and status.
4. Save updated product back via `ProductRepository`.
5. Save movement via `MovementRepository`.
6. Push movement onto the history `Stack`.
7. If `MovementType == DISPATCH`, enqueue movement to the dispatch `Queue`.

**`applyMovementToProduct` state machine:**

| Movement Type | Quantity change | New Status |
|---------------|----------------|------------|
| `INCOME` | `+ quantity` | `AVAILABLE` |
| `DISPATCH` | `- quantity` | `DISPATCHED` (if qty reaches 0) or `AVAILABLE` |
| `RETURN` | `+ quantity` | `RETURNED` |
| `CANCELLATION` | `- quantity` | `CANCELED` |
| `CORRECTION` | `± quantity` | `AVAILABLE` |

**`undoLastMovement` flow:**
1. Traverse the history `Stack` from top to bottom to find the last non-`CORRECTION` movement for the given `productId`.
2. Call `reverseMovementOnProduct(product, lastMovement)` — applies the inverse of the movement's effect.
3. Save the updated product.
4. Create a new `CORRECTION` movement referencing the undone movement's ID.
5. Save the correction movement and push it onto the stack.

**`getPendingDispatches`:** Converts the `Queue` to a `LinkedList` for UI display.

**`processNextDispatch`:** Calls `queue.dequeue()` to remove the next pending dispatch.

### 8.5 Exceptions

| Class | Extends | When thrown |
|-------|---------|-------------|
| `MovementDomainException` | `DomainException` | Generic movement rule violation |
| `MovementNotFoundException` | `MovementDomainException` | Movement ID not found |

---

## 9. Dependency Injection — CompositionRoot

File: `edu.inventory.administrator.services.CompositionRoot`

`CompositionRoot` is a **singleton** implemented with the initialization-on-demand holder pattern:

```java
public class CompositionRoot {
    private static class Holder {
        static final CompositionRoot INSTANCE = new CompositionRoot();
    }
    public static CompositionRoot getInstance() { return Holder.INSTANCE; }

    private final ProductRepository productRepository;
    private final ProductDomainService productService;
    private final MovementRepository movementRepository;
    private final InventoryDomainService inventoryService;
    // ... accessors
}
```

The private constructor instantiates all concrete implementations and wires them together. No external DI framework is used. To add a new dependency, instantiate it in the constructor and expose an accessor.

---

## 10. UI Layer

Package root: `edu.inventory.administrator.ui`

### 10.1 `MainFrame`

A `JFrame` (1280 × 760 px) containing a single `JTabbedPane` with three tabs. It is the only place that holds references to all three panels. It passes refresh callbacks at construction time.

### 10.2 `ProductsPanel`

**Form fields:** Product ID, Name, Category, Quantity, Supplier, Location, Status (dropdown).

**Buttons:**

| Button | Action |
|--------|--------|
| Save Product | Create new product or update existing one |
| Delete by ID | Remove product from repository by ID |
| Clear | Reset all form fields |
| Reload Table | Re-fetch all products from service |

**Table columns:** ID, Name, Category, Quantity, Created At, Supplier, Location, Status.

**Row click:** Populates the form with the selected product's data for quick editing.

**Validation:** ID and Name must be non-empty; Quantity must be a non-negative integer.

### 10.3 `MovementsPanel`

**Form fields:** Product (dropdown populated from service), Movement Type (dropdown), Quantity, Reason (text area).

**Buttons:**

| Button | Action |
|--------|--------|
| Register Movement | Calls `InventoryDomainService.registerMovement()` |
| Undo Last | Calls `InventoryDomainService.undoLastMovement()` for selected product |
| View by Product | Filters right-side table for selected product |
| Reload | Refreshes both tables and product dropdown |

**Two tables side-by-side:**
- Left: all movements across all products.
- Right: movements filtered by the product currently selected in the dropdown.

### 10.4 `DispatchQueuePanel`

Displays the current state of the `Queue<ProductMovement>` (FIFO order, top = next to process).

**Buttons:**

| Button | Action |
|--------|--------|
| Process Next Dispatch | Calls `processNextDispatch()`, then reloads |
| Reload Queue | Re-fetches queue contents via `getPendingDispatches()` |

**Table columns:** Movement ID, Product ID, Quantity, Created At, Reason.

### 10.5 Support Classes

**`UiMessages`** — Static convenience wrappers around `JOptionPane`:
```java
UiMessages.info(parentComponent, "Operation successful");
UiMessages.error(parentComponent, "Product not found");
```

**`Formatters`** — Static formatting helpers for display:
```java
Formatters.dateTime(localDateTime);  // "yyyy-MM-dd HH:mm:ss"
Formatters.text(string);             // Trims and normalizes null to ""
```

---

## 11. Entry Point

File: `edu.inventory.administrator.application.MainApplication`

```java
public class MainApplication {
    public static void main(String[] args) {
        // Set native look-and-feel
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        SwingUtilities.invokeLater(() -> {
            CompositionRoot root = CompositionRoot.getInstance();
            MainFrame frame = new MainFrame(root);
            frame.setVisible(true);
        });
    }
}
```

Key notes:
- The GUI is always launched on the **Event Dispatch Thread (EDT)** via `invokeLater`.
- `CompositionRoot` is initialized before `MainFrame` so all services are ready.
- The system look-and-feel is applied for a native appearance per OS.

---

## 12. Building and Running

### Prerequisites

- JDK 17+
- Maven 3.6+ (or `javac` / `java` directly)

### With Maven (recommended)

```bash
# Compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="edu.inventory.administrator.application.MainApplication"

# Or compile and run in one step
mvn clean compile exec:java -Dexec.mainClass="edu.inventory.administrator.application.MainApplication"
```

### With javac / java

```bash
# Compile all sources
javac -d out $(find src/main/java -name "*.java")

# Run
java -cp out edu.inventory.administrator.application.MainApplication
```

### Build Output

- Maven: compiled classes in `target/classes/`
- javac: compiled classes in `out/`

There is no packaging step (no JAR/WAR); the project is run directly from compiled classes.

---

## 13. Business Logic Reference

### Product Lifecycle

```
[Create] → AVAILABLE
              │
              ├─ INCOME       → AVAILABLE  (quantity increases)
              ├─ DISPATCH     → AVAILABLE  (if stock remains)
              │               → DISPATCHED (if stock hits 0)
              ├─ RETURN       → RETURNED   (quantity increases)
              ├─ CANCELLATION → CANCELED   (quantity decreases)
              └─ CORRECTION   → AVAILABLE  (quantity adjusted ± by undo)
```

### Undo Rules

- Only the **last non-CORRECTION** movement for a given product can be undone.
- Undoing creates a `CORRECTION` movement; it cannot itself be undone.
- The history `Stack` is global — it contains movements from all products interleaved. The undo traversal searches for the most recent matching `productId`.

### Dispatch Queue Rules

- Only `DISPATCH` movements enter the queue, automatically on registration.
- Queue is FIFO; `processNextDispatch` always removes the oldest pending dispatch.
- Processing a dispatch only removes it from the queue; it does not alter product data.

---

## 14. Data Persistence Model

**All data is in-memory and is lost when the application closes.**

| Store | Type | Backed by | Purpose |
|-------|------|-----------|---------|
| Products | `LinkedList<Product>` | `ProductRepositoryImpl` | Master product list |
| Movements | `LinkedList<ProductMovement>` | `MovementRepositoryImpl` | Full movement history |
| History stack | `Stack<ProductMovement>` | `InventoryDomainServiceImpl` | Enables undo |
| Dispatch queue | `Queue<ProductMovement>` | `InventoryDomainServiceImpl` | Pending FIFO dispatches |

To add file or database persistence, implement a new class for `ProductRepository` and/or `MovementRepository` and wire it in `CompositionRoot`.

---

## 15. Error Handling

Domain errors surface as unchecked exceptions that extend `DomainException`. The UI catches these and shows a dialog via `UiMessages.error()`.

| Exception | Trigger |
|-----------|---------|
| `ProductDomainException` | Duplicate product ID on create |
| `ProductNotFoundException` | Product ID not found in repository |
| `MovementDomainException` | Invalid quantity (≤ 0) or business rule violation |
| `MovementNotFoundException` | Movement ID not found |

No checked exceptions are used. All domain exceptions carry a descriptive message suitable for display.

---

## 16. Known Limitations and Future Improvements

| Area | Current State | Suggested Improvement |
|------|--------------|----------------------|
| Persistence | In-memory only | Add SQLite / file serialization via new Repository implementations |
| Testing | No tests exist | Add JUnit 5 unit tests for domain services and data structures |
| IDs | Manually typed strings | Auto-generate UUIDs to prevent collisions |
| Concurrency | Not thread-safe | Add synchronization to repositories if background tasks are added |
| Undo scope | Only last movement | Track full undo stack per product for multi-level undo |
| Console UI | `ProductsMenu`, `MovementsMenu` still exist but are unused | Remove or fully integrate as an alternative CLI mode |
| Validation | Minimal (non-empty, positive int) | Add max quantity limits, category constraints, etc. |
| Look and feel | System default only | Allow theme selection |
