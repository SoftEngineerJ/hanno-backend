package com.hannomed.backend.admin.controller;

import com.hannomed.backend.admin.service.AdminEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class AdminEmployeeController {

    private final AdminEmployeeService adminEmployeeService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllEmployees() {
        return ResponseEntity.ok(adminEmployeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEmployeeById(@PathVariable Integer id) {
        Map<String, Object> employee = adminEmployeeService.getEmployeeById(id);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employee);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createEmployee(@RequestBody Map<String, String> data) {
        try {
            return ResponseEntity.ok(adminEmployeeService.createEmployee(data));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateEmployee(
            @PathVariable Integer id,
            @RequestBody Map<String, String> data) {
        Map<String, Object> employee = adminEmployeeService.updateEmployee(id, data);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEmployee(@PathVariable Integer id) {
        try {
            boolean deleted = adminEmployeeService.deleteEmployee(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Mitarbeiter gelöscht"));
            }
            return ResponseEntity.status(404).body(Map.of("success", false, "error", "Mitarbeiter nicht gefunden"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Map<String, Object>> hardDeleteEmployee(@PathVariable Integer id) {
        try {
            boolean deleted = adminEmployeeService.hardDeleteEmployee(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Mitarbeiter endgültig gelöscht"));
            }
            return ResponseEntity.status(404).body(Map.of("success", false, "error", "Mitarbeiter nicht gefunden"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreEmployee(@PathVariable Integer id) {
        try {
            boolean restored = adminEmployeeService.restoreEmployee(id);
            if (restored) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Mitarbeiter wiederhergestellt"));
            }
            return ResponseEntity.status(404).body(Map.of("success", false, "error", "Mitarbeiter nicht gefunden"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, Object>> updatePassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> data) {
        try {
            String currentPassword = data.get("currentPassword");
            String newPassword = data.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Passwörter erforderlich"));
            }

            boolean updated = adminEmployeeService.updatePassword(id, currentPassword, newPassword);
            if (updated) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Passwort geändert"));
            }
            return ResponseEntity.status(400).body(Map.of("success", false, "error", "Aktuelles Passwort ist falsch"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getEmployeeCount() {
        return ResponseEntity.ok(Map.of("count", adminEmployeeService.getEmployeeCount()));
    }
}
