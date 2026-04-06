package com.hannomed.backend.service;

import com.hannomed.backend.entity.Employee;
import com.hannomed.backend.repository.EmployeeRepository;
import com.hannomed.backend.service.BrevoEmailService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final BrevoEmailService brevoEmailService;

    @Value("${jwt.secret:hanno-admin-secret-key-for-production-2024}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public Map<String, Object> login(String username, String password) {
        Optional<Employee> employeeOpt = employeeRepository.findByUsername(username);

        if (employeeOpt.isEmpty()) {
            employeeOpt = employeeRepository.findByEmail(username);
        }

        if (employeeOpt.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        Employee employee = employeeOpt.get();

        if (employee.getDeletedAt() != null) {
            throw new RuntimeException("Account has been deleted");
        }

        if (!passwordEncoder.matches(password, employee.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Generate JWT token
        String token = generateJwtToken(employee);

        Map<String, Object> response = new HashMap<>();
        response.put("id", employee.getId());
        response.put("email", employee.getEmail());
        response.put("username", employee.getUsername());
        response.put("firstName", employee.getFirstName());
        response.put("lastName", employee.getLastName());
        response.put("role", employee.getRole());
        response.put("profilePhotoUrl", employee.getProfilePhotoUrl());
        response.put("token", token);

        return response;
    }

    private String generateJwtToken(Employee employee) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(employee.getId().toString())
                .claim("username", employee.getUsername())
                .claim("firstName", employee.getFirstName() != null ? employee.getFirstName() : "")
                .claim("lastName", employee.getLastName() != null ? employee.getLastName() : "")
                .claim("email", employee.getEmail())
                .claim("role", employee.getRole() != null ? employee.getRole() : "USER")
                .claim("deletedAt", employee.getDeletedAt() != null ? employee.getDeletedAt().toString() : null)
                .claim("roles", employee.getRole() != null && employee.getRole().equals("admin")
                        ? java.util.List.of("ROLE_ADMIN")
                        : java.util.List.of("ROLE_USER"))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public void updateFcmToken(Integer employeeId, String fcmToken) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setFcmToken(fcmToken);
        employeeRepository.save(employee);
    }

    public boolean resetPassword(String email) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);

        if (employeeOpt.isEmpty()) {
            return false;
        }

        Employee employee = employeeOpt.get();

        if (employee.getDeletedAt() != null) {
            return false;
        }

        String newPassword = brevoEmailService.generatePassword();
        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);

        brevoEmailService.sendResetPasswordEmail(
                employee.getEmail(),
                employee.getFirstName(),
                employee.getUsername(),
                newPassword);

        return true;
    }
}