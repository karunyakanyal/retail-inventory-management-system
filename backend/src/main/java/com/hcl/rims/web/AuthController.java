package com.hcl.rims.web;

import com.hcl.rims.model.User;
import com.hcl.rims.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("authenticated", true);
        if (user != null) {
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().name());
        } else {
            response.put("role", authentication.getAuthorities().toString());
        }

        return ResponseEntity.ok(response);
    }
}
