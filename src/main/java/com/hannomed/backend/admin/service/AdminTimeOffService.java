package com.hannomed.backend.admin.service;

import com.hannomed.backend.entity.TimeOffRequest;
import com.hannomed.backend.entity.Employee;
import com.hannomed.backend.repository.TimeOffRequestRepository;
import com.hannomed.backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminTimeOffService {

    private final TimeOffRequestRepository timeOffRequestRepository;
    private final EmployeeRepository employeeRepository;

    private String getEmployeeName(Integer employeeId) {
        if (employeeId == null)
            return "Unbekannt";
        return employeeRepository.findById(employeeId)
                .map(e -> e.getFirstName() + " " + e.getLastName())
                .orElse("Unbekannt");
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
                    request.setUpdatedAt(LocalDateTime.now());
                    timeOffRequestRepository.save(request);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean rejectRequest(Integer requestId, String adminName) {
        return timeOffRequestRepository.findById(requestId)
                .map(request -> {
                    request.setStatus("abgelehnt");
                    request.setApprovedBy(adminName);
                    request.setUpdatedAt(LocalDateTime.now());
                    timeOffRequestRepository.save(request);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean cancelRequest(Integer requestId) {
        return timeOffRequestRepository.findById(requestId)
                .map(request -> {
                    request.setStatus("storniert");
                    request.setUpdatedAt(LocalDateTime.now());
                    timeOffRequestRepository.save(request);
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
                    request.setUpdatedAt(LocalDateTime.now());
                    timeOffRequestRepository.save(request);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean rejectCancellation(Integer requestId, String adminName) {
        return timeOffRequestRepository.findById(requestId)
                .map(request -> {
                    // Zurück auf genehmigt setzen
                    request.setStatus("genehmigt");
                    request.setApprovedBy(adminName);
                    request.setUpdatedAt(LocalDateTime.now());
                    timeOffRequestRepository.save(request);
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
        dto.put("employeeName", getEmployeeName(request.getEmployeeId()));
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
        return dto;
    }
}
