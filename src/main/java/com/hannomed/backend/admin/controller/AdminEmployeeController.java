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
        return ResponseEntity.ok(adminEmployeeService.createEmployee(data));
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
        boolean deleted = adminEmployeeService.deleteEmployee(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Mitarbeiter gelöscht"));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getEmployeeCount() {
        return ResponseEntity.ok(Map.of("count", adminEmployeeService.getEmployeeCount()));
    }
}
