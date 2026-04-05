package com.hannomed.backend.service;

import com.hannomed.backend.entity.Employee;
import com.hannomed.backend.repository.EmployeeRepository;
import com.hannomed.backend.service.BrevoEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final BrevoEmailService brevoEmailService;

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

        Map<String, Object> response = new HashMap<>();
        response.put("id", employee.getId());
        response.put("email", employee.getEmail());
        response.put("username", employee.getUsername());
        response.put("firstName", employee.getFirstName());
        response.put("lastName", employee.getLastName());
        response.put("role", employee.getRole());
        response.put("profilePhotoUrl", employee.getProfilePhotoUrl());

        return response;
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