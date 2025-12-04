package com.example.oauth2security.config;

import com.example.oauth2security.model.Permission;
import com.example.oauth2security.model.Role;
import com.example.oauth2security.model.User;
import com.example.oauth2security.repository.PermissionRepository;
import com.example.oauth2security.repository.RoleRepository;
import com.example.oauth2security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create Permissions
        Permission userRead = createPermissionIfNotExists("USER_READ", "Read user information");
        Permission userCreate = createPermissionIfNotExists("USER_CREATE", "Create new users");
        Permission userUpdate = createPermissionIfNotExists("USER_UPDATE", "Update user information");
        Permission userDelete = createPermissionIfNotExists("USER_DELETE", "Delete users");

        Permission productRead = createPermissionIfNotExists("PRODUCT_READ", "Read product information");
        Permission productCreate = createPermissionIfNotExists("PRODUCT_CREATE", "Create new products");
        Permission productUpdate = createPermissionIfNotExists("PRODUCT_UPDATE", "Update product information");
        Permission productDelete = createPermissionIfNotExists("PRODUCT_DELETE", "Delete products");

        Permission reportRead = createPermissionIfNotExists("REPORT_READ", "Read reports");
        Permission reportCreate = createPermissionIfNotExists("REPORT_CREATE", "Create reports");

        // Create Roles
        Role adminRole = createRoleIfNotExists("ADMIN", "Administrator with all permissions",
            userRead, userCreate, userUpdate, userDelete,
            productRead, productCreate, productUpdate, productDelete,
            reportRead, reportCreate
        );

        Role managerRole = createRoleIfNotExists("MANAGER", "Manager with read/write permissions",
            userRead, userUpdate,
            productRead, productCreate, productUpdate,
            reportRead, reportCreate
        );

        Role userRole = createRoleIfNotExists("USER", "Regular user with read-only permissions",
            userRead, productRead, reportRead
        );

        // Create Users
        createUserIfNotExists("admin", "admin123", true, adminRole);
        createUserIfNotExists("manager", "manager123", true, managerRole);
        createUserIfNotExists("user", "user123", true, userRole);

        System.out.println("=================================================");
        System.out.println("Data initialization completed!");
        System.out.println("=================================================");
        System.out.println("Sample Users:");
        System.out.println("  - admin/admin123 (ADMIN role with all permissions)");
        System.out.println("  - manager/manager123 (MANAGER role with read/write permissions)");
        System.out.println("  - user/user123 (USER role with read-only permissions)");
        System.out.println("=================================================");
    }

    private Permission createPermissionIfNotExists(String name, String description) {
        return permissionRepository.findByName(name)
            .orElseGet(() -> {
                Permission permission = new Permission(name, description);
                return permissionRepository.save(permission);
            });
    }

    private Role createRoleIfNotExists(String name, String description, Permission... permissions) {
        return roleRepository.findByName(name)
            .orElseGet(() -> {
                Role role = new Role(name, description);
                role.setPermissions(new HashSet<>(Arrays.asList(permissions)));
                return roleRepository.save(role);
            });
    }

    private User createUserIfNotExists(String username, String password, boolean enabled, Role... roles) {
        return userRepository.findByUsername(username)
            .orElseGet(() -> {
                User user = new User(username, passwordEncoder.encode(password), enabled);
                user.setRoles(new HashSet<>(Arrays.asList(roles)));
                return userRepository.save(user);
            });
    }
}
