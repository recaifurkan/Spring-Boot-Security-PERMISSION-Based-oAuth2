package com.example.authserver.config;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final Map<String, User.UserBuilder> users = new HashMap<>();

    public CustomUserDetailsService(PasswordEncoder encoder) {

        // Listeyi istediğin gibi doldur
        users.put("ahmet",
                User.withUsername("ahmet")
                        .password(encoder.encode("12345"))
                        .authorities("ROLE_USER", "SCOPE_product.read", "SCOPE_product.write")
        );

        users.put("mehmet",
                User.withUsername("mehmet")
                        .password(encoder.encode("12345"))
                        .authorities("ROLE_USER")
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = users.get(username).build();
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return user;
    }
}
