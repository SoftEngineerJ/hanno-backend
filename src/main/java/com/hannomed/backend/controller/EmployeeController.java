package com.hannomed.backend.controller;

import com.hannomed.backend.entity.Employee;
import com.hannomed.backend.repository.EmployeeRepository;
import com.hannomed.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final AuthService authService;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/{employeeId}")
    public ResponseEntity<Map<String, Object>> getEmployeeProfile(@PathVariable Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .map(employee -> {
                    Map<String, Object> profile = Map.of(
                            "id", employee.getId(),
                            "email", employee.getEmail() != null ? employee.getEmail() : "",
                            "username", employee.getUsername() != null ? employee.getUsername() : "",
                            "firstName", employee.getFirstName() != null ? employee.getFirstName() : "",
                            "lastName", employee.getLastName() != null ? employee.getLastName() : "",
                            "role", employee.getRole() != null ? employee.getRole() : "",
                            "profilePhotoUrl",
                            employee.getProfilePhotoUrl() != null ? employee.getProfilePhotoUrl() : "",
                            "position", employee.getPosition() != null ? employee.getPosition() : "",
                            "tourNumber", employee.getTourNumber() != null ? employee.getTourNumber() : "",
                            "standort", employee.getStandort() != null ? employee.getStandort() : "");
                    return ResponseEntity.ok(profile);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{employeeId}/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            @PathVariable Integer employeeId,
            @RequestBody Map<String, String> body) {

        String fcmToken = body.get("fcmToken");
        authService.updateFcmToken(employeeId, fcmToken);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .map(employee -> {
                    employee.setDeletedAt(java.time.LocalDateTime.now());
                    employee.setDeletedBy("self");
                    employee.setDeleteReason("self_delete");
                    employeeRepository.save(employee);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}