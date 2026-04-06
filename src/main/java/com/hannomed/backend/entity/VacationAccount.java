package com.hannomed.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "vacation_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacationAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "vacation_entitlement")
    private Integer vacationEntitlement = 30;

    @Column(name = "carried_over")
    private Integer carriedOver = 0;

    @Column(name = "carried_over_expiry")
    private LocalDate carriedOverExpiry;

    @Column(name = "initial_used_days")
    private Integer initialUsedDays = 0;

    @Column(name = "special_leave_initial")
    private Integer specialLeaveInitial = 0;

    @Column(name = "compensation_initial")
    private Integer compensationInitial = 0;

    @Column(name = "created_at", columnDefinition = "timestamp")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "timestamp")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Berlin"));
        createdAt = now;
        updatedAt = now;
        // Set carry-over expiry to March 31 of current year
        if (carriedOverExpiry == null && carriedOver != null && carriedOver > 0) {
            carriedOverExpiry = LocalDate.of(year, 3, 31);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.of("Europe/Berlin"));
    }
}
