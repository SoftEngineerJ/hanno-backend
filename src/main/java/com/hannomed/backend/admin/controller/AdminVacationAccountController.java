package com.hannomed.backend.admin.controller;

import com.hannomed.backend.entity.VacationAccount;
import com.hannomed.backend.repository.EmployeeRepository;
import com.hannomed.backend.service.VacationAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/vacation-accounts")
@RequiredArgsConstructor
public class AdminVacationAccountController {

    private final VacationAccountService vacationAccountService;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/employee/{employeeId}/year/{year}")
    public ResponseEntity<Map<String, Object>> getAccount(
            @PathVariable Integer employeeId,
            @PathVariable Integer year) {
        Map<String, Object> account = vacationAccountService.getAccountWithCalculations(employeeId, year);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<VacationAccount>> getEmployeeHistory(
            @PathVariable Integer employeeId) {
        List<VacationAccount> history = vacationAccountService.getEmployeeHistory(employeeId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<Map<String, Object>> getAllAccountsForYear(
            @PathVariable Integer year) {
        Map<String, Object> accounts = vacationAccountService.getAllAccountsForYear(year);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/employee/{employeeId}/year/{year}")
    public ResponseEntity<VacationAccount> updateInitialValues(
            @PathVariable Integer employeeId,
            @PathVariable Integer year,
            @RequestBody Map<String, Integer> body) {

        Integer initialUsedDays = body.get("initialUsedDays");
        Integer specialLeaveInitial = body.get("specialLeaveInitial");
        Integer compensationInitial = body.get("compensationInitial");
        Integer carriedOver = body.get("carriedOver");

        VacationAccount updated = vacationAccountService.updateInitialValues(
                employeeId, year, initialUsedDays, specialLeaveInitial, compensationInitial, carriedOver);

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/create-all/{year}")
    public ResponseEntity<Map<String, Object>> createAllAccountsForYear(@PathVariable Integer year) {
        List<Integer> employeeIds = employeeRepository.findAll().stream()
                .map(e -> e.getId())
                .toList();

        int created = 0;
        for (Integer employeeId : employeeIds) {
            vacationAccountService.getOrCreateAccount(employeeId, year);
            created++;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("created", created);
        response.put("message", "Urlaubskonten für " + created + " Mitarbeiter erstellt");

        return ResponseEntity.ok(response);
    }
}
