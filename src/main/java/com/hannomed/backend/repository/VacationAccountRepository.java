package com.hannomed.backend.repository;

import com.hannomed.backend.entity.VacationAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VacationAccountRepository extends JpaRepository<VacationAccount, Integer> {

    List<VacationAccount> findByEmployeeIdOrderByYearDesc(Integer employeeId);

    Optional<VacationAccount> findByEmployeeIdAndYear(Integer employeeId, Integer year);

    boolean existsByEmployeeIdAndYear(Integer employeeId, Integer year);

    List<VacationAccount> findByYear(Integer year);

    void deleteByEmployeeId(Integer employeeId);
}
