package com.hannomed.backend.admin.controller;

import com.hannomed.backend.admin.service.AdminTimeOffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/timeoff")
@RequiredArgsConstructor
public class AdminTimeOffController {

    private final AdminTimeOffService adminTimeOffService;

    @GetMapping("/requests")
    public ResponseEntity<List<Map<String, Object>>> getAllRequests() {
        return ResponseEntity.ok(adminTimeOffService.getAllRequests());
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingRequests() {
        return ResponseEntity.ok(adminTimeOffService.getAllPendingRequests());
    }

    @GetMapping("/requests/status/{status}")
    public ResponseEntity<List<Map<String, Object>>> getRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(adminTimeOffService.getRequestsByStatus(status));
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<Map<String, Object>> getRequestById(@PathVariable Integer id) {
        Map<String, Object> request = adminTimeOffService.getRequestById(id);
        if (request == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(request);
    }

    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveRequest(@PathVariable Integer id) {
        boolean success = adminTimeOffService.approveRequest(id);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Antrag genehmigt"));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectRequest(@PathVariable Integer id) {
        boolean success = adminTimeOffService.rejectRequest(id);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Antrag abgelehnt"));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/requests/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelRequest(@PathVariable Integer id) {
        boolean success = adminTimeOffService.cancelRequest(id);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Antrag storniert"));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(adminTimeOffService.getStatistics());
    }
}
