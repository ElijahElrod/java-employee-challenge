package com.reliaquest.api.service;

import com.reliaquest.api.constants.CacheNames;
import com.reliaquest.api.model.request.CreateEmployeeRequest;
import com.reliaquest.api.model.response.EmployeeResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EmployeeService provides business logic and caches results for employee operations.
 * <p>
 * Offers methods to:
 * <ul>
 *     <li>Retrieve all employees</li>
 *     <li>Search employees by name</li>
 *     <li>Get an employee by ID</li>
 *     <li>Get the highest salary among employees</li>
 *     <li>List the top ten highest earning employee names</li>
 *     <li>Create a new employee</li>
 *     <li>Delete an employee by ID</li>
 * </ul>
 * <p>
 * Uses {@link EmployeeClient} for API calls and uses Caffeine-based Spring Caching.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class EmployeeService {

    public static final int TEN = 10;
    private final EmployeeClient employeeClient;

    @PostConstruct
    @Profile("production")
    public void warmupCache() {
        try {
            final var initialEmployees = getAllEmployees();
            log.info("Warmed cache with [{}] entries", initialEmployees.size());
        } catch (final Throwable throwable) {
            log.warn("Failed to warm cache: [{}]", throwable.getLocalizedMessage());
        }
    }

    /**
     * Gets all the {}
     *
     * @return a list containing all employees
     */
    @Cacheable(CacheNames.EMPLOYEES)
    public List<EmployeeResponse> getAllEmployees() {
        return this.employeeClient.getAllEmployees();
    }

    @Cacheable(CacheNames.EMPLOYEES_BY_NAME_SEARCH)
    public List<EmployeeResponse> getEmployeesByNameSearch(final String searchString) {
        final var lowerSearchStr = searchString.toLowerCase();
        return getAllEmployees()
                .stream()
                .filter(employee -> employee.getName().toLowerCase().contains(lowerSearchStr))
                .toList();
    }


    /**
     * Gets the employe
     *
     * @param id
     * @return
     */
    @Cacheable(value = CacheNames.EMPLOYEE_BY_ID, key = "#id")
    public EmployeeResponse getEmployeeById(String id) {

        return this.employeeClient.getEmployeeById(id);
    }

    /**
     * Returns the highest salary of employees
     *
     * @return the highest employee salary, or 0 if no employee entries are present.
     */
    @Cacheable(CacheNames.TOP_SALARY)
    public Integer getHighestSalaryOfEmployees() {
        List<EmployeeResponse> employees = this.getAllEmployees();
        return employees.stream().map(EmployeeResponse::getSalary).max(Integer::compareTo).orElse(0);
    }


    @Cacheable(CacheNames.TOP_EARNING_EMPLOYEES)
    public List<String> getTopTenHighestEarningEmployeeNames() {

        List<EmployeeResponse> employees = this.getAllEmployees();
        return employees.stream().sorted(Comparator.comparingInt(EmployeeResponse::getSalary)).map(EmployeeResponse::getName).limit(TEN).collect(Collectors.toList());
    }


    /**
     * Creates a new employee, and adds it to the cache via the id of the {@link EmployeeResponse}. Invalidates salary-based queries as the new employee could be present for the new results.
     *
     * @param employeeInput The employee object to create
     * @return a {@link EmployeeResponse}
     */
    @Caching(
            put = @CachePut(value = {CacheNames.EMPLOYEE_BY_ID}, key = "#result.id"),
            evict = @CacheEvict(value = {CacheNames.EMPLOYEES, CacheNames.TOP_SALARY, CacheNames.TOP_EARNING_EMPLOYEES, CacheNames.EMPLOYEES_BY_NAME_SEARCH}, allEntries = true)
    )
    public EmployeeResponse createEmployee(CreateEmployeeRequest employeeInput) {
        return employeeClient.createEmployee(employeeInput);
    }


    /**
     * Deletes the employee specified by the provided id
     *
     * @param id The id for the employee object to delete
     * @return true if delete was successful, false otherwise
     */
    @Caching(
            evict = {
                    @CacheEvict(value = {CacheNames.EMPLOYEE_BY_ID}, key = "#id"),
                    @CacheEvict(value = {CacheNames.EMPLOYEES, CacheNames.TOP_SALARY, CacheNames.TOP_EARNING_EMPLOYEES, CacheNames.EMPLOYEES_BY_NAME_SEARCH}, allEntries = true)
            }
    )
    public boolean deleteEmployeeById(String id) {

        return employeeClient.deleteEmployeeById(id);
    }

}
