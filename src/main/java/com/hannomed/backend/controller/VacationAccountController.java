package com.hannomed.backend.controller;

import com.hannomed.backend.entity.VacationAccount;
import com.hannomed.backend.service.VacationAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vacation-accounts")
@RequiredArgsConstructor
public class VacationAccountController {

    private final VacationAccountService vacationAccountService;

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
        
        VacationAccount updated = vacationAccountService.updateInitialValues(
                employeeId, year, initialUsedDays, specialLeaveInitial, compensationInitial);
        
        return ResponseEntity.ok(updated);
    }
}
