package com.hannomed.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/admin")
public class EventController {

    private static final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

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
    public ResponseEntity<SseEmitter> streamEvents() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        return ResponseEntity.ok(emitter);
    }
}
