package com.hannomed.backend.admin.controller;

import com.hannomed.backend.admin.service.AdminManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/admins")
public class AdminManagementController {

    private final AdminManagementService adminManagementService;

    public AdminManagementController(AdminManagementService adminManagementService) {
        this.adminManagementService = adminManagementService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllAdmins() {
        return ResponseEntity.ok(adminManagementService.getAllAdmins());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAdminById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminManagementService.getAdminById(id));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAdmin(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(adminManagementService.createAdmin(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAdmin(@PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        try {
            return ResponseEntity.ok(adminManagementService.updateAdmin(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAdmin(@PathVariable Integer id) {
        return ResponseEntity.ok(adminManagementService.deleteAdmin(id));
    }
}
