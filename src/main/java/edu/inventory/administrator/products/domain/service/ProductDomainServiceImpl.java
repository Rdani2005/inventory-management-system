package edu.inventory.administrator.products.domain.service;

import edu.inventory.administrator.datastructures.LinkedList;
import edu.inventory.administrator.products.domain.entity.Product;
import edu.inventory.administrator.products.domain.exception.ProductDomainException;
import edu.inventory.administrator.products.domain.exception.ProductNotFoundException;
import edu.inventory.administrator.products.domain.service.repository.ProductRepository;
import edu.inventory.administrator.products.domain.valueobject.ProductId;
import java.util.Objects;

public class ProductDomainServiceImpl implements ProductDomainService {
    private final ProductRepository repository;

    public ProductDomainServiceImpl(ProductRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public void createProduct(Product product) {
        Objects.requireNonNull(product, "product must not be null");
        if (exists(product.getId())) {
            throw new ProductDomainException("A product with the same id already exists.");
        }
        repository.saveProduct(product);
    }

    @Override
    public Product getProduct(ProductId id) {
        Product product = repository.getProduct(id);
        if (product == null) {
            throw new ProductNotFoundException("Product was not found.");
        }
        return product;
    }

    @Override
    public LinkedList<Product> getAllProducts() {
        return repository.getAllProducts();
    }

    @Override
    public void deleteProduct(ProductId id) {
        getProduct(id);
        repository.deleteProduct(id);
    }

    @Override
    public boolean exists(ProductId id) {
        return repository.getProduct(id) != null;
    }
}
