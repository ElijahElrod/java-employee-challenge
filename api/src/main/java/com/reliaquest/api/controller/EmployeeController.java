package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.request.CreateEmployeeRequest;
import com.reliaquest.api.model.response.EmployeeResponse;
import com.reliaquest.api.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;


/**
 * EmployeeController handles HTTP requests related to employee operations.
 * <p>
 * This controller provides endpoints for:
 * <ul>
 *     <li>Retrieving all employees</li>
 *     <li>Searching employees by name fragment</li>
 *     <li>Getting an employee by ID</li>
 *     <li>Getting the highest salary among employees</li>
 *     <li>Listing the top ten highest earning employee names</li>
 *     <li>Creating a new employee</li>
 *     <li>Deleting an employee by ID</li>
 * </ul>
 * <p>
 * Uses {@link EmployeeService} for business logic and {@link ObjectMapper} for request conversion.
 * <p>
 */
@Log4j2
@RestController
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<EmployeeResponse, CreateEmployeeRequest> {

    private final ObjectMapper objectMapper;
    private final EmployeeService employeeService;

    @Override
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        log.info("Getting all employees");
        final List<EmployeeResponse> employees = this.employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByNameSearch(String searchString) {
        log.info("Searching employees that match search param: [{}]", searchString);
        final List<EmployeeResponse> matchedEmployees = this.employeeService.getEmployeesByNameSearch(searchString);
        return ResponseEntity.ok(matchedEmployees);
    }

    @Override
    public ResponseEntity<EmployeeResponse> getEmployeeById(String id) {
        log.info("Searching employee by id: [{}]", id);
        final EmployeeResponse employeeResponse = this.employeeService.getEmployeeById(id);
        if (employeeResponse != null) {
            return ResponseEntity.ok(employeeResponse);
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        final int topSalary = this.employeeService.getHighestSalaryOfEmployees();
        return ResponseEntity.ok(topSalary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        final List<String> topEarningEmployees = employeeService.getTopTenHighestEarningEmployeeNames();
        return ResponseEntity.ok(topEarningEmployees);
    }

    @Override
    public ResponseEntity<EmployeeResponse> createEmployee(CreateEmployeeRequest employeeInput) {
        final CreateEmployeeRequest createEmployeeRequest = objectMapper.convertValue(employeeInput, CreateEmployeeRequest.class);
        final EmployeeResponse createdEmployee = this.employeeService.createEmployee(createEmployeeRequest);
        final URI location = URI.create("/" + createdEmployee.getId());
        return ResponseEntity.created(location).body(createdEmployee);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        if (this.employeeService.deleteEmployeeById(id)) {
            // TODO: Maybe convert this to be an 200
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.internalServerError().body("Could not delete employee with id: " + id);
    }
}
