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
        System.out.println(">>> EventController.broadcastNewRequest called: " + employeeName + " type: " + type);
        System.out.println(">>> Active emitters: " + emitters.size());
        String data = String.format("{\"type\":\"new_request\",\"employeeName\":\"%s\",\"requestType\":\"%s\"}",
                employeeName, type);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("new-request")
                        .data(data));
                System.out.println(">>> Sent to emitter");
            } catch (IOException e) {
                System.out.println(">>> Error sending to emitter: " + e.getMessage());
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
