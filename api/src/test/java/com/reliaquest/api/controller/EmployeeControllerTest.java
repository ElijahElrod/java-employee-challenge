package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.request.CreateEmployeeRequest;
import com.reliaquest.api.model.response.EmployeeResponse;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class EmployeeControllerTest {

    private EmployeeService employeeService;
    private ObjectMapper objectMapper;
    private EmployeeController employeeController;

    @BeforeEach
    void setUp() {
        employeeService = mock(EmployeeService.class);
        objectMapper = new ObjectMapper();
        employeeController = new EmployeeController(objectMapper, employeeService);
    }

    @Test
    void testGetAllEmployees() {
        List<EmployeeResponse> mockList = Arrays.asList(
                new EmployeeResponse(UUID.randomUUID(), "Alice", 100000, 30, "Engineer", "alice@example.com"),
                new EmployeeResponse(UUID.randomUUID(), "Bob", 90000, 28, "Analyst", "bob@example.com")
        );
        when(employeeService.getAllEmployees()).thenReturn(mockList);

        ResponseEntity<List<EmployeeResponse>> response = employeeController.getAllEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockList, response.getBody());
    }

    @Test
    void testGetEmployeesByNameSearch() {
        String search = "Ali";
        List<EmployeeResponse> mockList = List.of(
                new EmployeeResponse(UUID.randomUUID(), "Alice", 100000, 30, "Engineer", "alice@example.com")
        );
        when(employeeService.getEmployeesByNameSearch(search)).thenReturn(mockList);

        ResponseEntity<List<EmployeeResponse>> response = employeeController.getEmployeesByNameSearch(search);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockList, response.getBody());
    }

    @Test
    void testGetEmployeeByIdFound() {
        UUID id = UUID.randomUUID();
        EmployeeResponse mockEmployee = new EmployeeResponse(id, "Alice", 100000, 30, "Engineer", "alice@example.com");
        when(employeeService.getEmployeeById(id.toString())).thenReturn(mockEmployee);

        ResponseEntity<EmployeeResponse> response = employeeController.getEmployeeById(id.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockEmployee, response.getBody());
    }

    @Test
    void testGetEmployeeByIdNotFound() {
        when(employeeService.getEmployeeById("notfound")).thenReturn(null);

        ResponseEntity<EmployeeResponse> response = employeeController.getEmployeeById("notfound");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetHighestSalaryOfEmployees() {
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(120_000);

        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(120_000, response.getBody());
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNames() {
        List<String> names = List.of("Alice", "Bob", "Charlie");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(names);

        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(names, response.getBody());
    }

    @Test
    void testCreateEmployee() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setName("Alice");
        request.setSalary(100000);
        request.setAge(30);
        request.setTitle("Engineer");

        EmployeeResponse created = new EmployeeResponse(UUID.randomUUID(), "Alice", 100000, 30, "Engineer", "alice@example.com");
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(created);

        ResponseEntity<EmployeeResponse> response = employeeController.createEmployee(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(created, response.getBody());
        assertTrue(Objects.requireNonNull(response.getHeaders().getLocation()).toString().endsWith(created.getId().toString()));
    }

    @Test
    void testDeleteEmployeeByIdSuccess() {
        when(employeeService.deleteEmployeeById("123")).thenReturn(true);

        ResponseEntity<String> response = employeeController.deleteEmployeeById("123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testDeleteEmployeeByIdFailure() {
        when(employeeService.deleteEmployeeById("123")).thenReturn(false);

        ResponseEntity<String> response = employeeController.deleteEmployeeById("123");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Could not delete employee with id: 123", response.getBody());
    }
}