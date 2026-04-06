package com.hannomed.backend.service;

import com.hannomed.backend.entity.VacationAccount;
import com.hannomed.backend.entity.TimeOffRequest;
import com.hannomed.backend.repository.VacationAccountRepository;
import com.hannomed.backend.repository.TimeOffRequestRepository;
import com.hannomed.backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VacationAccountService {

    private final VacationAccountRepository vacationAccountRepository;
    private final TimeOffRequestRepository timeOffRequestRepository;
    private final EmployeeRepository employeeRepository;

    private static final int DEFAULT_VACATION_ENTITLEMENT = 30;

    public VacationAccount getOrCreateAccount(Integer employeeId, Integer year) {
        Optional<VacationAccount> existing = vacationAccountRepository
                .findByEmployeeIdAndYear(employeeId, year);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new account for the year
        VacationAccount newAccount = new VacationAccount();
        newAccount.setEmployeeId(employeeId);
        newAccount.setYear(year);
        newAccount.setVacationEntitlement(DEFAULT_VACATION_ENTITLEMENT);
        newAccount.setCarriedOver(0);
        newAccount.setInitialUsedDays(0);
        newAccount.setSpecialLeaveInitial(0);
        newAccount.setCompensationInitial(0);

        // Check for carry-over from previous year
        Optional<VacationAccount> previousYearAccount = vacationAccountRepository
                .findByEmployeeIdAndYear(employeeId, year - 1);

        if (previousYearAccount.isPresent()) {
            VacationAccount prev = previousYearAccount.get();
            LocalDate today = LocalDate.now();

            // Calculate remaining vacation from previous year
            int prevEntitlement = prev.getVacationEntitlement() != null ? prev.getVacationEntitlement()
                    : DEFAULT_VACATION_ENTITLEMENT;
            int prevCarriedOver = prev.getCarriedOver() != null ? prev.getCarriedOver() : 0;
            int prevInitialUsed = prev.getInitialUsedDays() != null ? prev.getInitialUsedDays() : 0;

            // Get approved requests for previous year
            LocalDate prevYearStart = LocalDate.of(year - 1, 1, 1);
            LocalDate prevYearEnd = LocalDate.of(year - 1, 12, 31);
            List<TimeOffRequest> prevYearRequests = timeOffRequestRepository
                    .findByEmployeeIdAndStartDateBetweenOrderByStartDateDesc(prev.getEmployeeId(), prevYearStart,
                            prevYearEnd);

            int prevUsedRequests = prevYearRequests.stream()
                    .filter(req -> "genehmigt".equals(req.getStatus()))
                    .mapToInt(to -> to.getRequestedDays() != null ? to.getRequestedDays() : 0)
                    .sum();

            int totalPrevVacation = prevEntitlement + prevCarriedOver;
            int totalPrevUsed = prevInitialUsed + prevUsedRequests;
            int remainingFromPrevYear = Math.max(0, totalPrevVacation - totalPrevUsed);

            // Check if carry-over has expired (March 31)
            if (today.isAfter(LocalDate.of(year, 3, 31))) {
                // Carry-over expired
                newAccount.setCarriedOver(0);
            } else {
                // Use remaining from previous year as carry-over
                newAccount.setCarriedOver(remainingFromPrevYear);
                newAccount.setCarriedOverExpiry(LocalDate.of(year, 3, 31));
            }
        }

        return vacationAccountRepository.save(newAccount);
    }

    public VacationAccount createAccountWithInitialValues(Integer employeeId, Integer year,
            Integer initialUsedDays, Integer specialLeaveInitial, Integer compensationInitial, Integer carriedOver) {

        VacationAccount account = getOrCreateAccount(employeeId, year);

        if (initialUsedDays != null) {
            account.setInitialUsedDays(initialUsedDays);
        }
        if (specialLeaveInitial != null) {
            account.setSpecialLeaveInitial(specialLeaveInitial);
        }
        if (compensationInitial != null) {
            account.setCompensationInitial(compensationInitial);
        }
        if (carriedOver != null) {
            account.setCarriedOver(carriedOver);
            account.setCarriedOverExpiry(LocalDate.of(year, 3, 31));
        }

        return vacationAccountRepository.save(account);
    }

    public Map<String, Object> getAccountWithCalculations(Integer employeeId, Integer year) {
        VacationAccount account = getOrCreateAccount(employeeId, year);

        // Calculate used days from approved requests for this year
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);

        List<TimeOffRequest> yearRequests = timeOffRequestRepository
                .findByEmployeeIdOrderByStartDateDesc(employeeId);

        int usedVacationDays = 0;
        int usedSpecialLeave = 0;
        int usedCompensation = 0;

        for (TimeOffRequest req : yearRequests) {
            if (!"genehmigt".equals(req.getStatus())) {
                continue;
            }

            // Check if request is within the year
            if (req.getEndDate().isBefore(yearStart) || req.getStartDate().isAfter(yearEnd)) {
                continue;
            }

            int days = req.getRequestedDays() != null ? req.getRequestedDays() : 0;

            switch (req.getType()) {
                case "Urlaub":
                    usedVacationDays += days;
                    break;
                case "Sonderurlaub":
                    usedSpecialLeave += days;
                    break;
                case "Freizeitausgleich":
                    usedCompensation += days;
                    break;
            }
        }

        // Calculate totals - add vacation_entitlement + carried_over
        int totalVacation = (account.getVacationEntitlement() != null ? account.getVacationEntitlement()
                : DEFAULT_VACATION_ENTITLEMENT)
                + (account.getCarriedOver() != null ? account.getCarriedOver() : 0);
        int totalUsedVacation = (account.getInitialUsedDays() != null ? account.getInitialUsedDays() : 0)
                + usedVacationDays;
        int remainingVacation = totalVacation - totalUsedVacation;

        int totalSpecialLeave = (account.getSpecialLeaveInitial() != null ? account.getSpecialLeaveInitial() : 0)
                + usedSpecialLeave;
        int totalCompensation = (account.getCompensationInitial() != null ? account.getCompensationInitial() : 0)
                + usedCompensation;

        Map<String, Object> response = new HashMap<>();
        response.put("id", account.getId());
        response.put("employeeId", employeeId);
        response.put("year", year);
        response.put("vacationEntitlement", account.getVacationEntitlement());
        response.put("carriedOver", account.getCarriedOver());
        response.put("carriedOverExpiry", account.getCarriedOverExpiry());
        response.put("initialUsedDays", account.getInitialUsedDays());
        response.put("specialLeaveInitial", account.getSpecialLeaveInitial());
        response.put("compensationInitial", account.getCompensationInitial());
        response.put("totalVacationEntitlement", totalVacation);

        // Calculated values
        response.put("usedVacationDays", usedVacationDays);
        response.put("totalUsedVacation", totalUsedVacation);
        response.put("remainingVacation", remainingVacation);
        response.put("usedSpecialLeave", usedSpecialLeave);
        response.put("totalSpecialLeave", totalSpecialLeave);
        response.put("usedCompensation", usedCompensation);
        response.put("totalCompensation", totalCompensation);

        return response;
    }

    public VacationAccount updateInitialValues(Integer employeeId, Integer year,
            Integer initialUsedDays, Integer specialLeaveInitial, Integer compensationInitial, Integer carriedOver) {

        VacationAccount account = getOrCreateAccount(employeeId, year);

        if (initialUsedDays != null) {
            account.setInitialUsedDays(initialUsedDays);
        }
        if (specialLeaveInitial != null) {
            account.setSpecialLeaveInitial(specialLeaveInitial);
        }
        if (compensationInitial != null) {
            account.setCompensationInitial(compensationInitial);
        }
        if (carriedOver != null) {
            account.setCarriedOver(carriedOver);
            account.setCarriedOverExpiry(LocalDate.of(year, 3, 31));
        }

        return vacationAccountRepository.save(account);
    }

    public List<VacationAccount> getEmployeeHistory(Integer employeeId) {
        return vacationAccountRepository.findByEmployeeIdOrderByYearDesc(employeeId);
    }

    public Map<String, Object> getAllAccountsForYear(Integer year) {
        List<VacationAccount> accounts = vacationAccountRepository.findByYear(year);

        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("accounts", accounts);

        return response;
    }
}
