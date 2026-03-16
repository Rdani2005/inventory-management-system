package edu.inventory.administrator.products.domain.service.repository;

import edu.inventory.administrator.datastructures.LinkedList;
import edu.inventory.administrator.products.domain.entity.Product;
import edu.inventory.administrator.products.domain.valueobject.ProductId;
import java.util.Objects;

public class ProductRepositoryImpl implements ProductRepository {
    private final LinkedList<Product> products;

    public ProductRepositoryImpl() {
        this.products = new LinkedList<>();
    }

    @Override
    public void saveProduct(Product product) {
        Objects.requireNonNull(product, "product must not be null");
        boolean updated = products.update(current -> current.getId().equals(product.getId()), product);
        if (!updated) {
            products.add(product);
        }
    }

    @Override
    public Product getProduct(ProductId id) {
        if (id == null) {
            return null;
        }
        return products.find(product -> product.getId().equals(id));
    }

    @Override
    public LinkedList<Product> getAllProducts() {
        return products.copy();
    }

    @Override
    public void deleteProduct(ProductId id) {
        if (id != null) {
            products.remove(product -> product.getId().equals(id));
        }
    }
}
