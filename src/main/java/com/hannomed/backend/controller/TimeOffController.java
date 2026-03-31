package com.hannomed.backend.controller;

import com.hannomed.backend.service.TimeOffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/timeoff")
@RequiredArgsConstructor
public class TimeOffController {

    private final TimeOffService timeOffService;

    @GetMapping("/urlaubstatistik/{employeeId}")
    public ResponseEntity<Map<String, Object>> getUrlaubsstatistik(
            @PathVariable Integer employeeId,
            @RequestParam int jahr) {
        return ResponseEntity.ok(timeOffService.getUrlaubsstatistik(employeeId, jahr));
    }

    @GetMapping("/abwesenheitenliste/{employeeId}")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getAbwesenheitenListe(
            @PathVariable Integer employeeId) {
        return ResponseEntity.ok(timeOffService.getAbwesenheitenListe(employeeId));
    }

    @GetMapping("/abwesenheitenfuermonat/{employeeId}/{monatJahr}")
    public ResponseEntity<List<Map<String, Object>>> getAbwesenheitenFuerMonat(
            @PathVariable Integer employeeId,
            @PathVariable String monatJahr) {
        return ResponseEntity.ok(timeOffService.getAbwesenheitenFuerMonat(employeeId, monatJahr));
    }

    @PostMapping("/urlaubantrag")
    public ResponseEntity<Void> submitTimeOffRequest(@RequestBody Map<String, Object> body) {
        System.out.println(">>> POST /timeoff/urlaubantrag received: " + body);
        System.out.println(">>> Keys in body: " + body.keySet());
        timeOffService.submitTimeOffRequest(body);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stornieren/{id}")
    public ResponseEntity<Void> cancelTimeOff(@PathVariable Integer id) {
        timeOffService.cancelTimeOff(id);
        return ResponseEntity.ok().build();
    }
}
