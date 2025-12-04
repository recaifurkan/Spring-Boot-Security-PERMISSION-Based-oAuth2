package com.example.oauth2security.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public Map<String, Object> getAllProducts() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Getting all products");
        response.put("requiredPermission", "PRODUCT_READ");
        return response;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public Map<String, Object> getProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Getting product with id: " + id);
        response.put("requiredPermission", "PRODUCT_READ");
        return response;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public Map<String, Object> createProduct(@RequestBody Map<String, Object> productData) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Creating new product");
        response.put("requiredPermission", "PRODUCT_CREATE");
        response.put("productData", productData);
        return response;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public Map<String, Object> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> productData) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Updating product with id: " + id);
        response.put("requiredPermission", "PRODUCT_UPDATE");
        response.put("productData", productData);
        return response;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public Map<String, Object> deleteProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Deleting product with id: " + id);
        response.put("requiredPermission", "PRODUCT_DELETE");
        return response;
    }
}
