package com.hannomed.backend.admin.service;

import com.hannomed.backend.admin.entity.Admin;
import com.hannomed.backend.admin.repository.AdminRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret:hanno-admin-secret-key-for-production-2024}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

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

        String fullName = (admin.getFirstName() != null ? admin.getFirstName() : "") +
                (admin.getLastName() != null ? " " + admin.getLastName() : "");

        // JWT Token generieren
        String token = generateJwtToken(admin);

        return Map.of(
                "id", admin.getId(),
                "username", admin.getUsername() != null ? admin.getUsername() : "",
                "email", admin.getEmail() != null ? admin.getEmail() : "",
                "firstName", admin.getFirstName() != null ? admin.getFirstName() : "",
                "lastName", admin.getLastName() != null ? admin.getLastName() : "",
                "fullName", fullName.trim(),
                "role", admin.getRole() != null ? admin.getRole() : "admin",
                "token", token);
    }

    private String generateJwtToken(Admin admin) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        String fullName = (admin.getFirstName() != null ? admin.getFirstName() : "") +
                (admin.getLastName() != null ? " " + admin.getLastName() : "");

        return Jwts.builder()
                .subject(admin.getId().toString())
                .claim("username", admin.getUsername())
                .claim("firstName", admin.getFirstName() != null ? admin.getFirstName() : "")
                .claim("lastName", admin.getLastName() != null ? admin.getLastName() : "")
                .claim("fullName", fullName.trim())
                .claim("role", admin.getRole() != null ? admin.getRole() : "admin")
                .claim("roles", List.of("ROLE_" + (admin.getRole() != null ? admin.getRole().toUpperCase() : "ADMIN")))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public Optional<Admin> findById(Integer id) {
        return adminRepository.findById(id);
    }

    public Admin getAdminFromToken(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Integer adminId = Integer.parseInt(claims.getSubject());
            return adminRepository.findById(adminId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
