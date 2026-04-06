package com.hannomed.backend.admin.service;

import com.hannomed.backend.entity.TimeOffRequest;
import com.hannomed.backend.repository.TimeOffRequestRepository;
import com.hannomed.backend.repository.EmployeeRepository;
import com.hannomed.backend.service.PushNotificationService;
import com.hannomed.backend.service.BrevoEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminTimeOffService {

    private final TimeOffRequestRepository timeOffRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final PushNotificationService pushNotificationService;
    private final BrevoEmailService brevoEmailService;

    private String getEmployeeName(Integer employeeId) {
        if (employeeId == null)
            return "Unbekannt";
        return employeeRepository.findById(employeeId)
                .map(e -> e.getFirstName() + " " + e.getLastName())
                .orElse("Unbekannt");
    }

    private void sendStatusEmail(TimeOffRequest request, String status) {
        try {
            employeeRepository.findById(request.getEmployeeId()).ifPresent(employee -> {
                String startDate = request.getStartDate() != null ? request.getStartDate().toString() : "-";
                String endDate = request.getEndDate() != null ? request.getEndDate().toString() : "-";
                brevoEmailService.sendStatusChangeEmail(
                        employee.getEmail(),
                        employee.getFirstName(),
                        request.getType(),
                        status,
                        startDate,
                        endDate);
            });
        } catch (Exception e) {
            log.error("Failed to send status email: {}", e.getMessage());
        }
    }

    private void addHistory(TimeOffRequest request, String action, String adminName) {
        String currentHistory = request.getHistory();
        if (currentHistory == null)
            currentHistory = "";
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Berlin"));
        String entry = action + "|" + now.toString() + "|" + (adminName != null ? adminName : "") + ";";
        request.setHistory(currentHistory + entry);
    }

    private void sendPushNotification(Integer employeeId, String status, String type) {
        if (employeeId == null)
            return;

        employeeRepository.findById(employeeId).ifPresent(employee -> {
            String fcmToken = employee.getFcmToken();
            if (fcmToken != null && !fcmToken.isEmpty()) {
                String employeeName = employee.getFirstName();
                pushNotificationService.sendRequestStatusNotification(fcmToken, employeeName, status,
                        type != null ? type : "Antrag");
            }
        });
    }

    public List<Map<String, Object>> getAllPendingRequests() {
        List<TimeOffRequest> requests = timeOffRequestRepository.findByStatusOrderByCreatedAtDesc("wartend");
        return requests.stream().map(this::mapToDto).toList();
    }

    public List<Map<String, Object>> getAllRequests() {
        List<TimeOffRequest> requests = timeOffRequestRepository.findAllByOrderByCreatedAtDesc();
        return requests.stream().map(this::mapToDto).toList();
    }

    public List<Map<String, Object>> getRequestsByStatus(String status) {
        List<TimeOffRequest> requests = timeOffRequestRepository.findByStatusOrderByCreatedAtDesc(status);
        return requests.stream().map(this::mapToDto).toList();
    }

    @Transactional
    public boolean approveRequest(Integer requestId, String adminName) {
        return timeOffRequestRepository.findById(requestId)
                .map(request -> {
                    request.setStatus("genehmigt");
                    request.setApprovedBy(adminName);
                    request.setUpdatedAt(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
                    addHistory(request, "genehmigt", adminName);
                    timeOffRequestRepository.save(request);

                    sendPushNotification(request.getEmployeeId(), "genehmigt", request.getType());
                    sendStatusEmail(request, "genehmigt");

                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean rejectRequest(Integer requestId, String adminName, String rejectionReason) {
        return timeOffRequestRepository.findById(requestId)
                .map(request -> {
                    request.setStatus("abgelehnt");
                    request.setApprovedBy(adminName);
                    request.setRejectionReason(rejectionReason);
                    request.setUpdatedAt(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
                    addHistory(request, "abgelehnt", adminName);
                    timeOffRequestRepository.save(request);

                    sendPushNotification(request.getEmployeeId(), "abgelehnt", request.getType());
                    sendStatusEmail(request, "abgelehnt");

                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean cancelRequest(Integer requestId) {
        return timeOffRequestRepository.findById(requestId)
                .map(request -> {
                    request.setStatus("storniert");
                    request.setUpdatedAt(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
                    addHistory(request, "storniert", null);
                    timeOffRequestRepository.save(request);

                    sendStatusEmail(request, "storniert");

                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean confirmCancellation(Integer requestId, String adminName) {
        return timeOffRequestRepository.findById(requestId)
                .map(request -> {
                    request.setStatus("storniert");
                    request.setApprovedBy(adminName);
                    request.setUpdatedAt(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
                    addHistory(request, "storniert", adminName);
                    timeOffRequestRepository.save(request);

                    sendPushNotification(request.getEmployeeId(), "storniert", request.getType());
                    sendStatusEmail(request, "storniert");

                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean rejectCancellation(Integer requestId, String adminName) {
        return timeOffRequestRepository.findById(requestId)
                .map(request -> {
                    request.setStatus("genehmigt");
                    request.setApprovedBy(adminName);
                    request.setUpdatedAt(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
                    addHistory(request, "stornierung_abgelehnt", adminName);
                    timeOffRequestRepository.save(request);

                    sendStatusEmail(request, "stornierung_abgelehnt");

                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean resetStatus(Integer requestId, String adminName) {
        return timeOffRequestRepository.findById(requestId)
                .map(request -> {
                    request.setStatus("wartend");
                    request.setApprovedBy(adminName);
                    request.setRejectionReason(null);
                    request.setUpdatedAt(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
                    addHistory(request, "zurückgesetzt", adminName);
                    timeOffRequestRepository.save(request);

                    sendStatusEmail(request, "wartend");

                    return true;
                })
                .orElse(false);
    }

    public Map<String, Object> getRequestById(Integer id) {
        return timeOffRequestRepository.findById(id)
                .map(this::mapToDto)
                .orElse(null);
    }

    public Map<String, Object> getStatistics() {
        long pending = timeOffRequestRepository.countByStatus("wartend");
        long approved = timeOffRequestRepository.countByStatus("genehmigt");
        long rejected = timeOffRequestRepository.countByStatus("abgelehnt");
        long cancelled = timeOffRequestRepository.countByStatus("storniert");

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        stats.put("cancelled", cancelled);
        stats.put("total", pending + approved + rejected + cancelled);
        return stats;
    }

    private Map<String, Object> mapToDto(TimeOffRequest request) {
        Map<String, Object> dto = new java.util.HashMap<>();
        dto.put("id", request.getId());
        dto.put("employeeId", request.getEmployeeId());

        // Check if employee is deleted
        var employee = employeeRepository.findById(request.getEmployeeId()).orElse(null);
        boolean isEmployeeDeleted = employee != null && employee.getDeletedAt() != null;

        if (isEmployeeDeleted) {
            String originalName = getEmployeeName(request.getEmployeeId());
            dto.put("employeeName", originalName + " (Mitarbeiter gelöscht)");
            dto.put("employeeDeleted", true);
        } else {
            dto.put("employeeName", getEmployeeName(request.getEmployeeId()));
            dto.put("employeeDeleted", false);
        }

        // Add employee details
        if (employee != null) {
            dto.put("profilePhotoUrl", employee.getProfilePhotoUrl());
            dto.put("standort", employee.getStandort());
            dto.put("position", employee.getPosition());
        } else {
            dto.put("profilePhotoUrl", null);
            dto.put("standort", null);
            dto.put("position", null);
        }

        dto.put("type", request.getType() != null ? request.getType() : "");
        dto.put("reason", request.getType() != null ? request.getType() : "");
        dto.put("startDate", request.getStartDate() != null ? request.getStartDate().toString() : "");
        dto.put("endDate", request.getEndDate() != null ? request.getEndDate().toString() : "");
        dto.put("days", request.getRequestedDays());
        dto.put("requestedDays", request.getRequestedDays());
        dto.put("status", request.getStatus() != null ? request.getStatus() : "");
        dto.put("createdAt", request.getCreatedAt() != null ? request.getCreatedAt().toString() : "");
        dto.put("submittedAt", request.getCreatedAt() != null ? request.getCreatedAt().toString() : "");
        dto.put("updatedAt", request.getUpdatedAt() != null ? request.getUpdatedAt().toString() : "");
        dto.put("approvedBy", request.getApprovedBy() != null ? request.getApprovedBy() : "");
        dto.put("rejectionReason", request.getRejectionReason() != null ? request.getRejectionReason() : "");
        dto.put("history", request.getHistory() != null ? request.getHistory() : "");
        return dto;
    }
}
