package com.hannomed.backend.controller;

import com.hannomed.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            Map<String, Object> response = authService.login(username, password);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> data) {
        String email = data.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "E-Mail erforderlich"));
        }

        boolean success = authService.resetPassword(email);

        if (success) {
            return ResponseEntity
                    .ok(Map.of("success", true, "message", "E-Mail mit neuen Zugangsdaten wurde gesendet"));
        }
        return ResponseEntity
                .ok(Map.of("success", true, "message", "Wenn die E-Mail existiert, wurde eine E-Mail gesendet"));
    }
}