package com.hannomed.backend.admin.service;

import com.hannomed.backend.entity.Employee;
import com.hannomed.backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<Map<String, Object>> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getEmployeeById(Integer id) {
        return employeeRepository.findById(id)
                .map(this::mapToDto)
                .orElse(null);
    }

    public Map<String, Object> createEmployee(Map<String, String> data) {
        Employee employee = new Employee();
        employee.setEmail(data.get("email"));
        employee.setUsername(data.get("username"));
        employee.setPassword(data.get("password"));
        employee.setFirstName(data.get("firstName"));
        employee.setLastName(data.get("lastName"));
        employee.setRole(data.get("role") != null ? data.get("role") : "employee");
        employee.setPosition(data.get("position"));
        employee.setTourNumber(data.get("tourNumber"));
        employee.setStandort(data.get("standort"));

        if (data.containsKey("vacationDays") && data.get("vacationDays") != null) {
            employee.setVacationDays(Integer.parseInt(data.get("vacationDays")));
        } else {
            employee.setVacationDays(30);
        }

        if (data.containsKey("usedVacationDays") && data.get("usedVacationDays") != null) {
            employee.setUsedVacationDays(Integer.parseInt(data.get("usedVacationDays")));
        } else {
            employee.setUsedVacationDays(0);
        }

        if (data.containsKey("specialVacation") && data.get("specialVacation") != null) {
            employee.setSpecialVacation(Integer.parseInt(data.get("specialVacation")));
        } else {
            employee.setSpecialVacation(0);
        }

        if (data.containsKey("compensation") && data.get("compensation") != null) {
            employee.setCompensation(Integer.parseInt(data.get("compensation")));
        } else {
            employee.setCompensation(0);
        }

        Employee saved = employeeRepository.save(employee);
        return mapToDto(saved);
    }

    public Map<String, Object> updateEmployee(Integer id, Map<String, String> data) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    if (data.containsKey("email"))
                        employee.setEmail(data.get("email"));
                    if (data.containsKey("username"))
                        employee.setUsername(data.get("username"));
                    if (data.containsKey("password"))
                        employee.setPassword(data.get("password"));
                    if (data.containsKey("firstName"))
                        employee.setFirstName(data.get("firstName"));
                    if (data.containsKey("lastName"))
                        employee.setLastName(data.get("lastName"));
                    if (data.containsKey("role"))
                        employee.setRole(data.get("role"));
                    if (data.containsKey("position"))
                        employee.setPosition(data.get("position"));
                    if (data.containsKey("tourNumber"))
                        employee.setTourNumber(data.get("tourNumber"));
                    if (data.containsKey("standort"))
                        employee.setStandort(data.get("standort"));
                    if (data.containsKey("vacationDays") && data.get("vacationDays") != null)
                        employee.setVacationDays(Integer.parseInt(data.get("vacationDays")));
                    if (data.containsKey("usedVacationDays") && data.get("usedVacationDays") != null)
                        employee.setUsedVacationDays(Integer.parseInt(data.get("usedVacationDays")));
                    if (data.containsKey("specialVacation") && data.get("specialVacation") != null)
                        employee.setSpecialVacation(Integer.parseInt(data.get("specialVacation")));
                    if (data.containsKey("compensation") && data.get("compensation") != null)
                        employee.setCompensation(Integer.parseInt(data.get("compensation")));

                    Employee saved = employeeRepository.save(employee);
                    return mapToDto(saved);
                })
                .orElse(null);
    }

    public boolean deleteEmployee(Integer id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public long getEmployeeCount() {
        return employeeRepository.count();
    }

    private Map<String, Object> mapToDto(Employee employee) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", employee.getId());
        map.put("email", employee.getEmail() != null ? employee.getEmail() : "");
        map.put("username", employee.getUsername() != null ? employee.getUsername() : "");
        map.put("firstName", employee.getFirstName() != null ? employee.getFirstName() : "");
        map.put("lastName", employee.getLastName() != null ? employee.getLastName() : "");
        map.put("role", employee.getRole() != null ? employee.getRole() : "");
        map.put("position", employee.getPosition() != null ? employee.getPosition() : "");
        map.put("tourNumber", employee.getTourNumber() != null ? employee.getTourNumber() : "");
        map.put("standort", employee.getStandort() != null ? employee.getStandort() : "");
        map.put("profilePhotoUrl", employee.getProfilePhotoUrl() != null ? employee.getProfilePhotoUrl() : "");
        map.put("vacationDays", employee.getVacationDays() != null ? employee.getVacationDays() : 30);
        map.put("usedVacationDays", employee.getUsedVacationDays() != null ? employee.getUsedVacationDays() : 0);
        map.put("specialVacation", employee.getSpecialVacation() != null ? employee.getSpecialVacation() : 0);
        map.put("compensation", employee.getCompensation() != null ? employee.getCompensation() : 0);
        return map;
    }
}
