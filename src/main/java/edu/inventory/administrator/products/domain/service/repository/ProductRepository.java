package edu.inventory.administrator.products.domain.service.repository;

import edu.inventory.administrator.datastructures.LinkedList;
import edu.inventory.administrator.products.domain.entity.Product;
import edu.inventory.administrator.products.domain.valueobject.ProductId;

public interface ProductRepository {
    void saveProduct(Product product);
    Product getProduct(ProductId id);
    LinkedList<Product> getAllProducts();
    void deleteProduct(ProductId id);
}
