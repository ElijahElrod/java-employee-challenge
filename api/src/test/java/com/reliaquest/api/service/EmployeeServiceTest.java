package com.reliaquest.api.service;

import com.reliaquest.api.model.request.CreateEmployeeRequest;
import com.reliaquest.api.model.response.EmployeeResponse;

import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@EnableCaching
class EmployeeServiceTest {

    private static final String FILTER_KEY = "oe";
    private static final String TEST_UUID_STR = "67050f6d-c2a6-4a59-be61-a8479af074ba";
    private static final EmployeeResponse EMPLOYEE_RESPONSE = new EmployeeResponse(UUID.fromString(TEST_UUID_STR), "Alice Barnett", 105_000, 20, "Product Manager", "alice.barnett@gmail.com");


    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private EmployeeClient employeeClient;

    @BeforeEach
    public void init() {
        List<EmployeeResponse> employees = List.of(
                new EmployeeResponse(UUID.randomUUID(), "Jane Doe", 160_000, 30, "Engineering Manager", "jane.doe@gmail.com"),
                new EmployeeResponse(UUID.randomUUID(), "John Smith", 160_000, 30, "Engineering Manager", "john.smith@gmail.com")
        );

        when(employeeClient.getAllEmployees()).thenReturn(employees);
    }


    @Test
    void testGetEmployeesWithCaching() {

        // First call -> goes to client
        List<EmployeeResponse> firstCall = employeeService.getAllEmployees();

        // Second call -> should be cached (no new call to client)
        List<EmployeeResponse> secondCall = employeeService.getAllEmployees();
        List<EmployeeResponse> thirdCall = employeeService.getAllEmployees();

        assertThat(firstCall).isEqualTo(secondCall);
        assertThat(secondCall).isEqualTo(thirdCall);

        // Verify client called only once because of caching
        verify(employeeClient, times(2)).getAllEmployees();
    }

    @Test
    public void testGetEmployeesByNameSearch() {
        List<EmployeeResponse> cacheMiss = employeeService.getAllEmployees();

        // Should be Cache Hit Now, no sub-call to getAllEmployees
        List<EmployeeResponse> employeeResponses = employeeService.getEmployeesByNameSearch(FILTER_KEY);

        Assertions.assertEquals(1, employeeResponses.size());
        verify(employeeClient, times(1)).getAllEmployees();

    }

    @Test
    public void testCreateEmployeeCachesForFetchingEmployeeById() {
        CreateEmployeeRequest createEmployeeRequest = new CreateEmployeeRequest();
        createEmployeeRequest.setName("Jane Doe");
        createEmployeeRequest.setAge(30);
        createEmployeeRequest.setSalary(160_000);
        createEmployeeRequest.setTitle("Engineering Manager");

        when(employeeClient.createEmployee(createEmployeeRequest)).thenReturn(EMPLOYEE_RESPONSE);

        final var createdEmployee = employeeService.createEmployee(createEmployeeRequest);
        final var fetchedEmployee = employeeService.getEmployeeById(TEST_UUID_STR);

        Assertions.assertEquals(createdEmployee, fetchedEmployee);
        verify(employeeClient, times(0)).getEmployeeById(TEST_UUID_STR);

    }

}
