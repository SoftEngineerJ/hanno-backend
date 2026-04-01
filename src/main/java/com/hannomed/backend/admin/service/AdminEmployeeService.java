package com.hannomed.backend.admin.service;

import com.hannomed.backend.entity.Employee;
import com.hannomed.backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        Employee saved = employeeRepository.save(employee);
        return mapToDto(saved);
    }

    public Map<String, Object> updateEmployee(Integer id, Map<String, String> data) {
        return employeeRepository.findById(id)
            .map(employee -> {
                if (data.containsKey("email")) employee.setEmail(data.get("email"));
                if (data.containsKey("username")) employee.setUsername(data.get("username"));
                if (data.containsKey("password")) employee.setPassword(data.get("password"));
                if (data.containsKey("firstName")) employee.setFirstName(data.get("firstName"));
                if (data.containsKey("lastName")) employee.setLastName(data.get("lastName"));
                if (data.containsKey("role")) employee.setRole(data.get("role"));
                if (data.containsKey("position")) employee.setPosition(data.get("position"));
                if (data.containsKey("tourNumber")) employee.setTourNumber(data.get("tourNumber"));
                if (data.containsKey("standort")) employee.setStandort(data.get("standort"));

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
        return Map.of(
            "id", employee.getId(),
            "email", employee.getEmail() != null ? employee.getEmail() : "",
            "username", employee.getUsername() != null ? employee.getUsername() : "",
            "firstName", employee.getFirstName() != null ? employee.getFirstName() : "",
            "lastName", employee.getLastName() != null ? employee.getLastName() : "",
            "role", employee.getRole() != null ? employee.getRole() : "",
            "position", employee.getPosition() != null ? employee.getPosition() : "",
            "tourNumber", employee.getTourNumber() != null ? employee.getTourNumber() : "",
            "standort", employee.getStandort() != null ? employee.getStandort() : "",
            "profilePhotoUrl", employee.getProfilePhotoUrl() != null ? employee.getProfilePhotoUrl() : ""
        );
    }
}
