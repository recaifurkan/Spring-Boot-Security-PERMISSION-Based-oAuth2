package com.example.authserver.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('SCOPE_product.read')")
    public ResponseEntity<Map<String, String>> getProducts(@AuthenticationPrincipal Jwt jwt) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "OK");
        response.put("user", jwt.getClaimAsString("sub"));
        return ResponseEntity.ok(response);
    }
}

