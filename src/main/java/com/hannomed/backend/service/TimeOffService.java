package com.hannomed.backend.service;

import com.hannomed.backend.admin.repository.AdminRepository;
import com.hannomed.backend.controller.EventController;
import com.hannomed.backend.entity.Employee;
import com.hannomed.backend.entity.TimeOffRequest;
import com.hannomed.backend.entity.VacationAccount;
import com.hannomed.backend.repository.TimeOffRequestRepository;
import com.hannomed.backend.repository.EmployeeRepository;
import com.hannomed.backend.repository.VacationAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeOffService {

    private final TimeOffRequestRepository timeOffRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final VacationAccountRepository vacationAccountRepository;
    private final AdminRepository adminRepository;
    private final BrevoEmailService brevoEmailService;
    private static final int DEFAULT_URLAUBSANSPRUCH = 30;

    public Map<String, Object> getUrlaubsstatistik(Integer employeeId, int jahr) {
        LocalDate jahrStart = LocalDate.of(jahr, 1, 1);
        LocalDate jahrEnde = LocalDate.of(jahr, 12, 31);

        List<TimeOffRequest> anfragen = timeOffRequestRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);

        int genommeneTage = 0;
        int geplanteTage = 0;

        for (TimeOffRequest req : anfragen) {
            if (req.getEndDate().isBefore(jahrStart))
                continue;
            if (req.getStartDate().isAfter(jahrEnde))
                continue;

            if ("genehmigt".equals(req.getStatus())) {
                // Only count "Urlaub" for vacation days
                if ("Urlaub".equals(req.getType())) {
                    genommeneTage += req.getRequestedDays();
                }
            } else if ("wartend".equals(req.getStatus())) {
                // Only count "Urlaub" for planned days
                if ("Urlaub".equals(req.getType())) {
                    geplanteTage += req.getRequestedDays();
                }
            }
        }

        int urlaubsanspruch = DEFAULT_URLAUBSANSPRUCH;
        int initialUsedDays = 0;

        int carryOver = 0;
        Optional<VacationAccount> vacationAccount = vacationAccountRepository.findByEmployeeIdAndYear(employeeId, jahr);
        if (vacationAccount.isPresent()) {
            urlaubsanspruch = vacationAccount.get().getVacationEntitlement() != null
                    ? vacationAccount.get().getVacationEntitlement()
                    : DEFAULT_URLAUBSANSPRUCH;
            initialUsedDays = vacationAccount.get().getInitialUsedDays() != null
                    ? vacationAccount.get().getInitialUsedDays()
                    : 0;
            carryOver = vacationAccount.get().getCarriedOver() != null
                    ? vacationAccount.get().getCarriedOver()
                    : 0;
        }

        // vacation_entitlement + carryover - initialUsedDays - taken - planned
        int resturlaub = urlaubsanspruch + carryOver - initialUsedDays - genommeneTage - geplanteTage;
        int available = urlaubsanspruch + carryOver - initialUsedDays - genommeneTage;

        Map<String, Object> response = new HashMap<>();
        response.put("genommeneTage", genommeneTage + initialUsedDays);
        response.put("geplanteTage", geplanteTage);
        response.put("resturlaub", resturlaub);
        response.put("urlaubsanspruch", urlaubsanspruch);
        response.put("carryOver", carryOver);
        response.put("available", available);
        response.put("totalVacationEntitlement", urlaubsanspruch + carryOver);

        return response;
    }

    public Map<String, List<Map<String, Object>>> getAbwesenheitenListe(Integer employeeId) {
        List<TimeOffRequest> anfragen = timeOffRequestRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);

        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("wartend", new java.util.ArrayList<>());
        result.put("genehmigt", new java.util.ArrayList<>());
        result.put("abgelehnt", new java.util.ArrayList<>());
        result.put("storniert", new java.util.ArrayList<>());
        result.put("stornierung_beantragt", new java.util.ArrayList<>());

        for (TimeOffRequest req : anfragen) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", req.getId());
            item.put("typ", req.getType());
            item.put("startDatum", req.getStartDate().toString());
            item.put("endDatum", req.getEndDate().toString());
            item.put("requestedDays", req.getRequestedDays());
            item.put("status", req.getStatus());
            item.put("createdAt", req.getCreatedAt() != null ? req.getCreatedAt().toString() : null);
            item.put("updatedAt", req.getUpdatedAt() != null ? req.getUpdatedAt().toString() : null);
            item.put("approvedBy", req.getApprovedBy() != null ? req.getApprovedBy() : "");
            item.put("rejectionReason", req.getRejectionReason() != null ? req.getRejectionReason() : "");

            String status = req.getStatus();
            if (result.containsKey(status)) {
                result.get(status).add(item);
            }
        }

        return result;
    }

    public List<Map<String, Object>> getAbwesenheitenFuerMonat(Integer employeeId, String monatJahr) {
        // Handle both "03-2026" and "03.2026" formats
        String normalized = monatJahr.replace(".", "-");
        String[] parts = normalized.split("-");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);

        LocalDate monatStart = LocalDate.of(year, month, 1);
        LocalDate monatEnde = monatStart.withDayOfMonth(monatStart.lengthOfMonth());

        // Get ALL requests for this employee and filter in Java
        List<TimeOffRequest> allRequests = timeOffRequestRepository
                .findByEmployeeIdOrderByStartDateDesc(employeeId);

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TimeOffRequest req : allRequests) {
            // Check if request overlaps with the month
            if (req.getStartDate().isAfter(monatEnde) || req.getEndDate().isBefore(monatStart)) {
                continue; // Skip requests that don't overlap with the month
            }

            Map<String, Object> item = new HashMap<>();
            item.put("id", req.getId());
            item.put("typ", req.getType());
            item.put("startDatum", req.getStartDate().format(formatter));
            item.put("endDatum", req.getEndDate().format(formatter));
            item.put("requestedDays", req.getRequestedDays());
            item.put("status", req.getStatus());
            item.put("createdAt", req.getCreatedAt() != null ? req.getCreatedAt().format(formatter) : null);
            result.add(item);
        }

        return result;
    }

    public void submitTimeOffRequest(Map<String, Object> body) {
        Integer employeeId = (Integer) body.get("employeeId");

        // Verify employee exists and is not deleted
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Mitarbeiter nicht gefunden"));

        System.out.println(">>> DEBUG: Employee " + employeeId + " deletedAt = " + employee.getDeletedAt());

        if (employee.getDeletedAt() != null) {
            System.out.println(">>> DEBUG: Employee is deleted, throwing error!");
            throw new RuntimeException("Konto wurde gelöscht. Bitte melden Sie sich erneut an.");
        }

        TimeOffRequest request = new TimeOffRequest();
        request.setEmployeeId(employeeId);
        request.setType((String) body.get("type"));

        // Parse dates - accept both dd.MM.yyyy and yyyy-MM-dd formats
        String startDateStr = (String) body.get("startDate");
        String endDateStr = (String) body.get("endDate");

        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter germanFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        LocalDate startDate;
        LocalDate endDate;

        try {
            startDate = LocalDate.parse(startDateStr, isoFormatter);
        } catch (Exception e) {
            startDate = LocalDate.parse(startDateStr, germanFormatter);
        }

        try {
            endDate = LocalDate.parse(endDateStr, isoFormatter);
        } catch (Exception e) {
            endDate = LocalDate.parse(endDateStr, germanFormatter);
        }

        // Check for overlapping requests
        List<TimeOffRequest> existingRequests = timeOffRequestRepository
                .findByEmployeeIdOrderByStartDateDesc(request.getEmployeeId());
        for (TimeOffRequest existing : existingRequests) {
            // Only check against non-cancelled requests
            if (!"storniert".equals(existing.getStatus()) && !"abgelehnt".equals(existing.getStatus())) {
                // Check if date ranges overlap
                if (!(endDate.isBefore(existing.getStartDate()) || startDate.isAfter(existing.getEndDate()))) {
                    throw new RuntimeException("existingRequest={status=" + existing.getStatus() +
                            ", startDate=" + existing.getStartDate() +
                            ", endDate=" + existing.getEndDate() + "}");
                }
            }
        }

        request.setStartDate(startDate);
        request.setEndDate(endDate);

        // Calculate requestedDays from date difference if not provided
        Object requestedDaysObj = body.get("requestedDays");
        if (requestedDaysObj != null) {
            request.setRequestedDays((Integer) requestedDaysObj);
        } else {
            // Calculate days between start and end (inclusive)
            long days = ChronoUnit.DAYS.between(
                    request.getStartDate(), request.getEndDate()) + 1;
            request.setRequestedDays((int) days);
        }

        request.setStatus("wartend");

        timeOffRequestRepository.save(request);

        // Broadcast to admin via SSE
        String employeeName = employeeRepository.findById(request.getEmployeeId())
                .map(e -> e.getFirstName() + " " + e.getLastName())
                .orElse("Unbekannt");
        System.out.println(">>> Broadcasting SSE event for: " + employeeName + " type: " + request.getType());
        EventController.broadcastNewRequest(employeeName, request.getType());

        // Send email to all admins
        sendAdminNotification("neuer_antrag", employeeName, request.getType(),
                request.getStartDate().toString(), request.getEndDate().toString(), request.getRequestedDays());
    }

    private void sendAdminNotification(String type, String employeeName, String requestType,
            String startDate, String endDate, Integer days) {
        try {
            List<com.hannomed.backend.admin.entity.Admin> admins = adminRepository.findAll();

            if (admins.isEmpty()) {
                log.warn("Keine Admins gefunden für E-Mail-Benachrichtigung");
                return;
            }

            String typeText;
            String typeEmoji;
            switch (type) {
                case "neuer_antrag":
                    typeText = "Neuer Antrag";
                    typeEmoji = "NEW";
                    break;
                case "stornierung_beantragt":
                    typeText = "Stornierung beantragt";
                    typeEmoji = "CANCEL";
                    break;
                case "stornierung_direct":
                    typeText = "Stornierung";
                    typeEmoji = "CANCEL";
                    break;
                default:
                    typeText = type;
                    typeEmoji = "INFO";
            }

            String requestTypeText;
            switch (requestType) {
                case "Urlaub":
                    requestTypeText = "Urlaub";
                    break;
                case "Sonderurlaub":
                    requestTypeText = "Sonderurlaub";
                    break;
                case "Freizeitausgleich":
                    requestTypeText = "Freizeitausgleich";
                    break;
                default:
                    requestTypeText = requestType;
            }

            String formattedStartDate = formatDateGerman(startDate);
            String formattedEndDate = formatDateGerman(endDate);

            String subject = typeEmoji + " " + typeText + " - " + employeeName + " (" + requestTypeText + ")";
            String htmlContent = "<html><body>" +
                    "<p>Hallo Admin,</p>" +
                    "<p>Ein neuer Antrag wurde eingereicht:</p>" +
                    "<div style='background-color: #e3f2fd; padding: 16px; border-radius: 8px; margin: 16px 0; border-left: 4px solid #2196F3;'>"
                    +
                    "<p style='margin: 0;'><strong>Mitarbeiter:</strong> " + employeeName + "</p>" +
                    "<p style='margin: 8px 0 0 0;'><strong>Antragstyp:</strong> " + requestTypeText + "</p>" +
                    "<p style='margin: 8px 0 0 0;'><strong>Zeitraum:</strong> " + formattedStartDate + " bis "
                    + formattedEndDate + "</p>" +
                    "<p style='margin: 8px 0 0 0;'><strong>Tage:</strong> " + days + "</p>" +
                    "<p style='margin: 8px 0 0 0;'><strong>Aktion:</strong> " + typeText + "</p>" +
                    "</div>" +
                    "<p>Bitte überprüfen Sie den Antrag im Admin-Portal.</p>" +
                    "<p>Viele Grüße,<br>HannoApp System</p>" +
                    "</body></html>";

            for (com.hannomed.backend.admin.entity.Admin admin : admins) {
                try {
                    brevoEmailService.sendEmail(
                            admin.getEmail(),
                            admin.getFirstName() != null ? admin.getFirstName() : "Admin",
                            subject,
                            htmlContent);
                    log.info("Admin-E-Mail gesendet an: {}", admin.getEmail());
                } catch (Exception e) {
                    log.error("Fehler beim Senden der E-Mail an Admin {}: {}", admin.getEmail(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Fehler bei Admin-Benachrichtigung: {}", e.getMessage());
        }
    }

    private String formatDateGerman(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || dateStr.equals("-"))
            return "-";
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            return dateStr;
        }
    }

    public void cancelTimeOff(Integer id) {
        TimeOffRequest request = timeOffRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // Verify employee still exists and is not deleted
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Mitarbeiter nicht gefunden"));

        if (employee.getDeletedAt() != null) {
            throw new RuntimeException("Konto wurde gelöscht. Bitte melden Sie sich erneut an.");
        }

        // Abgelehnte Anträge können nicht storniert werden
        if ("abgelehnt".equalsIgnoreCase(request.getStatus())) {
            throw new RuntimeException("Abgelehnte Anträge können nicht storniert werden");
        }

        // Wenn bereits genehmigt, dann Status = stornierung_beantragt (wartet auf
        // Bestätigung)
        if ("genehmigt".equalsIgnoreCase(request.getStatus())) {
            request.setStatus("stornierung_beantragt");

            // Broadcast cancellation request to admin
            String employeeName = employeeRepository.findById(request.getEmployeeId())
                    .map(e -> e.getFirstName() + " " + e.getLastName())
                    .orElse("Unbekannt");
            EventController.broadcastCancellationRequest(employeeName, request.getType());

            // Send email to admins - Stornierung beantragt
            sendAdminNotification("stornierung_beantragt", employeeName, request.getType(),
                    request.getStartDate().toString(), request.getEndDate().toString(), request.getRequestedDays());
        } else {
            // For wartend status, directly cancel and notify admin
            request.setStatus("storniert");
            String employeeName = employeeRepository.findById(request.getEmployeeId())
                    .map(e -> e.getFirstName() + " " + e.getLastName())
                    .orElse("Unbekannt");
            EventController.broadcastRequestCancelled(employeeName, request.getType());

            // Send email to admins - Stornierung direkt
            sendAdminNotification("stornierung_direct", employeeName, request.getType(),
                    request.getStartDate().toString(), request.getEndDate().toString(), request.getRequestedDays());
        }
        timeOffRequestRepository.save(request);
    }
}
