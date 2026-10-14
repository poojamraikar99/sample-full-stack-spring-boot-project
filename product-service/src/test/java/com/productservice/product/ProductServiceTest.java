package com.productservice.product;

import com.productservice.exception.DuplicateSkuException;
import com.productservice.exception.ProductNotFoundException;
import com.productservice.model.Product;
import com.productservice.model.ProductRequest;
import com.productservice.model.ProductResponse;
import com.productservice.service.ProductService;
import com.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductRequest request;

    @BeforeEach
    void setUp() {
        request = new ProductRequest("Keyboard", "KEY-01", "Mechanical keyboard", new BigDecimal("79.99"), 12);
    }

    @Test
    void findAllReturnsProductResponses() {
        when(productRepository.findAll()).thenReturn(List.of(product()));

        List<ProductResponse> result = productService.findAll();

        assertEquals(1, result.size());
        assertEquals("KEY-01", result.get(0).sku());
    }

    @Test
    void findByIdReturnsProductResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product()));

        ProductResponse result = productService.findById(1L);

        assertEquals("Keyboard", result.name());
    }

    @Test
    void findByIdThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.findById(42L));
    }

    @Test
    void createSavesProductWhenSkuIsAvailable() {
        when(productRepository.existsBySku("KEY-01")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse result = productService.create(request);

        assertEquals("KEY-01", result.sku());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createRejectsDuplicateSku() {
        when(productRepository.existsBySku("KEY-01")).thenReturn(true);

        assertThrows(DuplicateSkuException.class, () -> productService.create(request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateChangesProductWhenSkuIsAvailable() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product()));
        when(productRepository.existsBySkuAndIdNot("KEY-01", 1L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse result = productService.update(1L, request);

        assertEquals("Keyboard", result.name());
        assertEquals("KEY-01", result.sku());
    }

    @Test
    void updateRejectsSkuUsedByAnotherProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product()));
        when(productRepository.existsBySkuAndIdNot("KEY-01", 1L)).thenReturn(true);

        assertThrows(DuplicateSkuException.class, () -> productService.update(1L, request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteRemovesExistingProduct() {
        Product product = product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(productRepository).delete(product);
    }

    private Product product() {
        return new Product("Keyboard", "KEY-01", "Mechanical keyboard", new BigDecimal("79.99"), 12);
    }
}
