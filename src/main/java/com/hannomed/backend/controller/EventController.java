package com.hannomed.backend.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/admin")
public class EventController {

    private static final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Value("${jwt.secret:hanno-admin-secret-key-for-production-2024}")
    private String jwtSecret;

    public static void broadcastNewRequest(String employeeName, String type) {
        String data = String.format("{\"type\":\"new_request\",\"employeeName\":\"%s\",\"requestType\":\"%s\"}",
                employeeName, type);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("new-request")
                        .data(data));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    public static void broadcastCancellationRequest(String employeeName, String type) {
        String data = String.format(
                "{\"type\":\"cancellation_request\",\"employeeName\":\"%s\",\"requestType\":\"%s\"}",
                employeeName, type);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("cancellation-request")
                        .data(data));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    public static void broadcastRequestCancelled(String employeeName, String type) {
        String data = String.format("{\"type\":\"request_cancelled\",\"employeeName\":\"%s\",\"requestType\":\"%s\"}",
                employeeName, type);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("request-cancelled")
                        .data(data));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> streamEvents(@RequestParam(required = false) String token) {
        System.out.println("🔍 SSE Request - Token present: " + (token != null && !token.isEmpty()));
        if (token != null && !token.isEmpty()) {
            try {
                System.out.println("🔍 SSE Token length: " + token.length());
                System.out.println("🔍 SSE Token start: " + token.substring(0, Math.min(20, token.length())) + "...");

                Jwts.parser()
                        .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseSignedClaims(token);

                System.out.println("✅ SSE Token validation successful");
            } catch (Exception e) {
                System.out.println("❌ SSE Token validation failed: " + e.getMessage());
                return ResponseEntity.status(401).build();
            }
        } else {
            // No token provided - unauthorized
            System.out.println("❌ No SSE token provided");
            return ResponseEntity.status(401).build();
        }

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        return ResponseEntity.ok(emitter);
    }
}
