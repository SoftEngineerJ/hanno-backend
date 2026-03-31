package com.hannomed.backend.repository;

import com.hannomed.backend.entity.TimeOffRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeOffRequestRepository extends JpaRepository<TimeOffRequest, Integer> {
    List<TimeOffRequest> findByEmployeeIdOrderByStartDateDesc(Integer employeeId);
    List<TimeOffRequest> findByEmployeeIdAndStartDateBetweenOrderByStartDateDesc(
            Integer employeeId, java.time.LocalDate start, java.time.LocalDate end);
}
