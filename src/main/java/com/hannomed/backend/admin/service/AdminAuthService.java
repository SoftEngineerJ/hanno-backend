package com.hannomed.backend.admin.service;

import com.hannomed.backend.admin.entity.Admin;
import com.hannomed.backend.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> login(String username, String password) {
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);

        if (adminOpt.isEmpty()) {
            throw new RuntimeException("Admin nicht gefunden");
        }

        Admin admin = adminOpt.get();

        // Support both BCrypt and plain text passwords
        boolean passwordValid = false;
        if (admin.getPassword().startsWith("$2")) {
            passwordValid = passwordEncoder.matches(password, admin.getPassword());
        } else {
            passwordValid = admin.getPassword().equals(password);
        }

        if (!passwordValid) {
            throw new RuntimeException("Falsches Passwort");
        }

        return Map.of(
                "id", admin.getId(),
                "username", admin.getUsername() != null ? admin.getUsername() : "",
                "email", admin.getEmail() != null ? admin.getEmail() : "",
                "firstName", admin.getFirstName() != null ? admin.getFirstName() : "",
                "lastName", admin.getLastName() != null ? admin.getLastName() : "",
                "role", admin.getRole() != null ? admin.getRole() : "admin",
                "token", "admin-token-" + System.currentTimeMillis());
    }

    public Optional<Admin> findById(Integer id) {
        return adminRepository.findById(id);
    }
}
