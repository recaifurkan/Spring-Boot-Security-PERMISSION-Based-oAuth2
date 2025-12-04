package com.example.oauth2security.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public Map<String, Object> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Getting all users");
        response.put("requiredPermission", "USER_READ");
        return response;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public Map<String, Object> getUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Getting user with id: " + id);
        response.put("requiredPermission", "USER_READ");
        return response;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public Map<String, Object> createUser(@RequestBody Map<String, Object> userData) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Creating new user");
        response.put("requiredPermission", "USER_CREATE");
        response.put("userData", userData);
        return response;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Updating user with id: " + id);
        response.put("requiredPermission", "USER_UPDATE");
        response.put("userData", userData);
        return response;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Deleting user with id: " + id);
        response.put("requiredPermission", "USER_DELETE");
        return response;
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> getProfile(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return response;
    }
}
