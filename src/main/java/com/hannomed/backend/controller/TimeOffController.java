package com.hannomed.backend.controller;

import com.hannomed.backend.repository.EmployeeRepository;
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
    private final EmployeeRepository employeeRepository;

    @GetMapping("/urlaubstatistik/{employeeId}")
    public ResponseEntity<Map<String, Object>> getUrlaubsstatistik(
            @PathVariable Integer employeeId,
            @RequestParam int jahr) {
        return employeeRepository.findById(employeeId)
                .map(employee -> {
                    if (employee.getDeletedAt() != null) {
                        return ResponseEntity.status(401).<Map<String, Object>>build();
                    }
                    return ResponseEntity.ok(timeOffService.getUrlaubsstatistik(employeeId, jahr));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/abwesenheitenliste/{employeeId}")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getAbwesenheitenListe(
            @PathVariable Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .map(employee -> {
                    if (employee.getDeletedAt() != null) {
                        return ResponseEntity.status(401).<Map<String, List<Map<String, Object>>>>build();
                    }
                    return ResponseEntity.ok(timeOffService.getAbwesenheitenListe(employeeId));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/abwesenheitenfuermonat/{employeeId}/{monatJahr}")
    public ResponseEntity<List<Map<String, Object>>> getAbwesenheitenFuerMonat(
            @PathVariable Integer employeeId,
            @PathVariable String monatJahr) {
        return employeeRepository.findById(employeeId)
                .map(employee -> {
                    if (employee.getDeletedAt() != null) {
                        return ResponseEntity.status(401).<List<Map<String, Object>>>build();
                    }
                    return ResponseEntity.ok(timeOffService.getAbwesenheitenFuerMonat(employeeId, monatJahr));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/urlaubantrag")
    public ResponseEntity<?> submitTimeOffRequest(@RequestBody Map<String, Object> body) {
        try {
            timeOffService.submitTimeOffRequest(body);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stornieren/{id}")
    public ResponseEntity<?> cancelTimeOff(@PathVariable Integer id) {
        try {
            timeOffService.cancelTimeOff(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
