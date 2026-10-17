package com.productservice.service;

import com.productservice.model.ProductRequest;
import com.productservice.model.ProductResponse;
import com.productservice.exception.DuplicateSkuException;
import com.productservice.model.Product;
import com.productservice.exception.ProductNotFoundException;
import com.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(getProduct(id));
    }

    public ProductResponse create(ProductRequest request) {
        ensureSkuIsAvailable(request.sku(), null);
        Product product = new Product(
                request.name(),
                request.sku(),
                request.description(),
                request.price(),
                request.quantity()
        );
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProduct(id);
        ensureSkuIsAvailable(request.sku(), id);
        product.update(
                request.name(),
                request.sku(),
                request.description(),
                request.price(),
                request.quantity()
        );
        return ProductResponse.from(productRepository.save(product));
    }

    public void delete(Long id) {
        productRepository.delete(getProduct(id));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private void ensureSkuIsAvailable(String sku, Long currentProductId) {
        boolean alreadyUsed = currentProductId == null
                ? productRepository.existsBySku(sku)
                : productRepository.existsBySkuAndIdNot(sku, currentProductId);
        if (alreadyUsed) {
            throw new DuplicateSkuException(sku);
        }
    }
}
