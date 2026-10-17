package com.productservice.product;

import com.productservice.controller.ProductController;
import com.productservice.exception.DuplicateSkuException;
import com.productservice.exception.ProductNotFoundException;
import com.productservice.model.ProductRequest;
import com.productservice.model.ProductResponse;
import com.productservice.service.ProductService;
import com.productservice.util.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void listsProducts() throws Exception {
        when(productService.findAll()).thenReturn(List.of(productResponse(1L)));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("KEY-01"));
    }

    @Test
    void getsProductById() throws Exception {
        when(productService.findById(1L)).thenReturn(productResponse(1L));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    @Test
    void createsProductAndReturnsLocation() throws Exception {
        when(productService.create(org.mockito.ArgumentMatchers.any(ProductRequest.class)))
                .thenReturn(productResponse(1L));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/products/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void rejectsInvalidProductRequest() throws Exception {
        ProductRequest invalidRequest = new ProductRequest("", "", null, BigDecimal.ZERO, -1);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.quantity").exists());
    }

    @Test
    void returnsNotFoundForMissingProduct() throws Exception {
        when(productService.findById(42L)).thenThrow(new ProductNotFoundException(42L));

        mockMvc.perform(get("/api/products/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/api/products/42"));
    }

    @Test
    void returnsConflictForDuplicateSku() throws Exception {
        when(productService.create(org.mockito.ArgumentMatchers.any(ProductRequest.class)))
                .thenThrow(new DuplicateSkuException("KEY-01"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A product with SKU 'KEY-01' already exists."));
    }

    @Test
    void updatesProduct() throws Exception {
        when(productService.update(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(ProductRequest.class)))
                .thenReturn(productResponse(1L));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("KEY-01"));
    }

    @Test
    void deletesProduct() throws Exception {
        doNothing().when(productService).delete(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).delete(1L);
    }

    private ProductRequest validRequest() {
        return new ProductRequest("Keyboard", "KEY-01", "Mechanical keyboard", new BigDecimal("79.99"), 12);
    }

    private ProductResponse productResponse(Long id) {
        return new ProductResponse(id, "Keyboard", "KEY-01", "Mechanical keyboard", new BigDecimal("79.99"), 12);
    }
}
