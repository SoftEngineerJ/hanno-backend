package com.hannomed.backend.admin.service;

import com.hannomed.backend.admin.entity.Admin;
import com.hannomed.backend.admin.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminManagementService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminManagementService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Map<String, Object>> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public Map<String, Object> getAdminById(Integer id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin nicht gefunden"));
        return mapToDto(admin);
    }

    public Map<String, Object> createAdmin(Map<String, String> request) {
        if (adminRepository.findByUsername(request.get("username")).isPresent()) {
            throw new RuntimeException("Benutzername bereits vergeben");
        }

        Admin admin = new Admin();
        admin.setUsername(request.get("username"));
        admin.setEmail(request.get("email"));
        admin.setPassword(passwordEncoder.encode(request.get("password")));
        admin.setFirstName(request.get("firstName"));
        admin.setLastName(request.get("lastName"));
        admin.setRole(request.getOrDefault("role", "admin"));

        Admin saved = adminRepository.save(admin);
        return mapToDto(saved);
    }

    public Map<String, Object> updateAdmin(Integer id, Map<String, String> request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin nicht gefunden"));

        if (request.containsKey("username")) {
            admin.setUsername(request.get("username"));
        }
        if (request.containsKey("email")) {
            admin.setEmail(request.get("email"));
        }

        // Passwort ändern mit Verifikation
        if (request.containsKey("currentPassword") && request.containsKey("newPassword")) {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (!currentPassword.isEmpty() && !newPassword.isEmpty()) {
                if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
                    throw new RuntimeException("Aktuelles Passwort ist falsch");
                }
                admin.setPassword(passwordEncoder.encode(newPassword));
            }
        } else if (request.containsKey("password") && !request.get("password").isEmpty()) {
            admin.setPassword(passwordEncoder.encode(request.get("password")));
        }

        if (request.containsKey("firstName")) {
            admin.setFirstName(request.get("firstName"));
        }
        if (request.containsKey("lastName")) {
            admin.setLastName(request.get("lastName"));
        }
        if (request.containsKey("role")) {
            admin.setRole(request.get("role"));
        }

        Admin saved = adminRepository.save(admin);
        return mapToDto(saved);
    }

    public Map<String, Object> deleteAdmin(Integer id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin nicht gefunden"));

        adminRepository.delete(admin);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Admin gelöscht");
        return response;
    }

    private Map<String, Object> mapToDto(Admin admin) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", admin.getId());
        dto.put("username", admin.getUsername());
        dto.put("email", admin.getEmail());
        dto.put("firstName", admin.getFirstName());
        dto.put("lastName", admin.getLastName());
        dto.put("role", admin.getRole());
        dto.put("createdAt", admin.getCreatedAt() != null ? admin.getCreatedAt().toString() : "");
        return dto;
    }
}
