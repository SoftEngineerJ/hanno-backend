package com.hannomed.backend.service;

import com.hannomed.backend.controller.EventController;
import com.hannomed.backend.entity.TimeOffRequest;
import com.hannomed.backend.repository.TimeOffRequestRepository;
import com.hannomed.backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TimeOffService {

    private final TimeOffRequestRepository timeOffRequestRepository;
    private final EmployeeRepository employeeRepository;
    private static final int DEFAULT_URLAUBSANSPRUCH = 30;

    public Map<String, Object> getUrlaubsstatistik(Integer employeeId, int jahr) {
        LocalDate jahrStart = LocalDate.of(jahr, 1, 1);
        LocalDate jahrEnde = LocalDate.of(jahr, 12, 31);

        List<TimeOffRequest> anfragen = timeOffRequestRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);

        int genommeneTage = 0;
        int geplanteTage = 0;

        for (TimeOffRequest req : anfragen) {
            if ("genehmigt".equals(req.getStatus())) {
                if (req.getEndDate().isBefore(jahrStart))
                    continue;
                if (req.getStartDate().isAfter(jahrEnde))
                    continue;
                genommeneTage += req.getRequestedDays();
            } else if ("wartend".equals(req.getStatus())) {
                if (req.getEndDate().isBefore(jahrStart))
                    continue;
                if (req.getStartDate().isAfter(jahrEnde))
                    continue;
                geplanteTage += req.getRequestedDays();
            }
        }

        int urlaubsanspruch = DEFAULT_URLAUBSANSPRUCH;

        int carryOver = 0;
        if (employeeRepository.findById(employeeId).isPresent()) {
            carryOver = employeeRepository.findById(employeeId).get().getCarriedOverDays() != null
                    ? employeeRepository.findById(employeeId).get().getCarriedOverDays()
                    : 0;
        }

        int resturlaub = urlaubsanspruch + carryOver - genommeneTage - geplanteTage;
        int available = urlaubsanspruch + carryOver - genommeneTage;

        Map<String, Object> response = new HashMap<>();
        response.put("genommeneTage", genommeneTage);
        response.put("geplanteTage", geplanteTage);
        response.put("resturlaub", resturlaub);
        response.put("urlaubsanspruch", urlaubsanspruch);
        response.put("carryOver", carryOver);
        response.put("available", available);

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
        TimeOffRequest request = new TimeOffRequest();
        request.setEmployeeId((Integer) body.get("employeeId"));
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
        EventController.broadcastNewRequest(employeeName, request.getType());
    }

    public void cancelTimeOff(Integer id) {
        TimeOffRequest request = timeOffRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // Abgelehnte Anträge können nicht storniert werden
        if ("abgelehnt".equalsIgnoreCase(request.getStatus())) {
            throw new RuntimeException("Abgelehnte Anträge können nicht storniert werden");
        }

        // Wenn bereits genehmigt, dann Status = stornierung_beantragt (wartet auf
        // Bestätigung)
        if ("genehmigt".equalsIgnoreCase(request.getStatus())) {
            request.setStatus("stornierung_beantragt");
        } else {
            request.setStatus("storniert");
        }
        timeOffRequestRepository.save(request);
    }
}
