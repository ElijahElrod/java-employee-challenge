package com.reliaquest.api.service;

import static org.mockito.Mockito.*;

import com.reliaquest.api.constants.CacheNames;
import com.reliaquest.api.model.request.CreateEmployeeRequest;
import com.reliaquest.api.model.response.EmployeeResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Unit tests for {@link EmployeeService}
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@EnableCaching
class EmployeeServiceTest {

    private static final String FILTER_KEY = "oe";
    private static final String TEST_UUID_STR = "67050f6d-c2a6-4a59-be61-a8479af074ba";
    private static final EmployeeResponse EMPLOYEE_RESPONSE = new EmployeeResponse(
            UUID.fromString(TEST_UUID_STR), "Alice Barnett", 105_000, 20, "Product Manager", "alice.barnett@gmail.com");

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private EmployeeClient employeeClient;

    @BeforeEach
    public void init() {
        List<EmployeeResponse> employees = List.of(
                new EmployeeResponse(
                        UUID.randomUUID(), "Jane Doe", 160_000, 30, "Engineering Manager", "jane.doe@gmail.com"),
                new EmployeeResponse(
                        UUID.randomUUID(), "John Smith", 160_000, 30, "Engineering Manager", "john.smith@gmail.com"));

        when(employeeClient.getAllEmployees()).thenReturn(employees);
    }

    @Test
    void testGetEmployeesWithCaching() {

        // First call -> goes to client
        List<EmployeeResponse> firstCall = employeeService.getAllEmployees();

        // Second call -> should be cached (no new call to client)
        List<EmployeeResponse> secondCall = employeeService.getAllEmployees();
        List<EmployeeResponse> thirdCall = employeeService.getAllEmployees();

        Assertions.assertEquals(firstCall, secondCall);
        Assertions.assertEquals(secondCall, thirdCall);

        // Verify client called only once because of caching
        verify(employeeClient, times(1)).getAllEmployees();
    }

    @Test
    public void testGetEmployeesByNameSearch() {
        List<EmployeeResponse> cacheMiss = employeeService.getAllEmployees();

        // Should be Cache Hit Now, no sub-call to getAllEmployees
        List<EmployeeResponse> employeeResponses = employeeService.getEmployeesByNameSearch(FILTER_KEY);

        Assertions.assertEquals(1, employeeResponses.size());
        verify(employeeClient, times(1)).getAllEmployees();
        verifyCaffeineCacheKey(CacheNames.EMPLOYEES_BY_NAME_SEARCH, FILTER_KEY, cacheManager);
    }

    @Test
    public void testCreateEmployeeCachesForFetchingEmployeeById() {
        CreateEmployeeRequest createEmployeeRequest = new CreateEmployeeRequest();
        createEmployeeRequest.setName("Jane Doe");
        createEmployeeRequest.setAge(30);
        createEmployeeRequest.setSalary(160_000);
        createEmployeeRequest.setTitle("Engineering Manager");

        when(employeeClient.createEmployee(createEmployeeRequest)).thenReturn(EMPLOYEE_RESPONSE);
        when(employeeClient.getEmployeeById(TEST_UUID_STR)).thenReturn(EMPLOYEE_RESPONSE);

        final var createdEmployee = employeeService.createEmployee(createEmployeeRequest);
        final var fetchedEmployee = employeeService.getEmployeeById(TEST_UUID_STR);

        Assertions.assertEquals(createdEmployee, fetchedEmployee);
        verify(employeeClient, times(1)).getEmployeeById(TEST_UUID_STR);
        verifyCaffeineCacheKey(CacheNames.EMPLOYEE_BY_ID, TEST_UUID_STR, cacheManager);
    }

    @Test
    void testGetTopTenEarningEmployees() {
        // given
        List<EmployeeResponse> employees = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            EmployeeResponse emp = new EmployeeResponse();
            emp.setId(UUID.randomUUID());
            emp.setName("Employee" + i);
            emp.setSalary(i * 1000); // Employee1=1000, Employee20=20000
            emp.setAge(20 + i);
            emp.setTitle("Title" + i);
            emp.setEmail("employee" + i + "@example.com");
            employees.add(emp);
        }

        when(employeeClient.getAllEmployees()).thenReturn(employees);
        final List<String> topEarners = employeeService.getTopTenHighestEarningEmployeeNames();

        Assertions.assertEquals(10, topEarners.size());
        Assertions.assertEquals(
                List.of(
                        "Employee20",
                        "Employee19",
                        "Employee18",
                        "Employee17",
                        "Employee16",
                        "Employee15",
                        "Employee14",
                        "Employee13",
                        "Employee12",
                        "Employee11"),
                topEarners);
    }

    @Test
    void testGetHighestSalary() {
        // given
        List<EmployeeResponse> employees = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            EmployeeResponse emp = new EmployeeResponse();
            emp.setId(UUID.randomUUID());
            emp.setName("Employee" + i);
            emp.setSalary(i * 1000); // Employee1=1000, Employee20=20000
            emp.setAge(20 + i);
            emp.setTitle("Title" + i);
            emp.setEmail("employee" + i + "@example.com");
            employees.add(emp);
        }

        when(employeeClient.getAllEmployees()).thenReturn(employees);
        final int topSalary = employeeService.getHighestSalaryOfEmployees();

        Assertions.assertEquals(20_000, topSalary);
    }

    private void verifyCaffeineCacheKey(String cacheName, String cacheKey, CacheManager cacheManager) {
        Cache springCache = cacheManager.getCache(cacheName);
        if (springCache instanceof CaffeineCache caffeineCache) {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCaffeineCache =
                    (com.github.benmanes.caffeine.cache.Cache<Object, Object>) caffeineCache.getNativeCache();
            Assertions.assertNotNull(nativeCaffeineCache.getIfPresent(cacheKey));
        } else {
            System.out.println("Cache '" + cacheName + "' is not a CaffeineCache.");
            Assertions.fail();
        }
    }
}
