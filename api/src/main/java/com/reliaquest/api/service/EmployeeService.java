package com.reliaquest.api.service;

import com.reliaquest.api.constants.CacheNames;
import com.reliaquest.api.model.request.CreateEmployeeRequest;
import com.reliaquest.api.model.response.EmployeeResponse;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

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

    private static final int TEN = 10;
    private static final int NO_SALARY = 0;
    private final EmployeeClient employeeClient;
    private final EmployeeSearchCacheEvictionService employeeSearchCacheEvictionService;

    private final ConcurrentHashMap<UUID, Set<String>> employeeToSearchStrings = new ConcurrentHashMap<>();

    @PostConstruct
    @Profile("!test")
    public void warmupCache() {
        try {
            final var initialEmployees = getAllEmployees();
            log.info("Warmed cache with [{}] entries", initialEmployees.size());
        } catch (final Throwable throwable) {
            log.warn("Failed to warm cache: [{}]", throwable.getLocalizedMessage());
        }
    }

    /**
     * Gets all the employees.
     *
     * @return a collection containing all employees.
     */
    @Cacheable(CacheNames.EMPLOYEES)
    public List<EmployeeResponse> getAllEmployees() {
        return this.employeeClient.getAllEmployees();
    }

    /**
     * Gets employees matching the search string.
     *
     * @param searchString the search string to match on
     * @return the collection of {@link EmployeeResponse} that matched the search string.
     */
    @Cacheable(CacheNames.EMPLOYEES_BY_NAME_SEARCH)
    public List<EmployeeResponse> getEmployeesByNameSearch(final String searchString) {
        final var lowerSearchStr = searchString.toLowerCase();
        final var matchedEmployees = getAllEmployees().stream()
                .filter(employee -> employee.getName().toLowerCase().contains(lowerSearchStr))
                .toList();

        // Build reverse index to evict entries more efficiently than dropping entire cache
        for (final var employee : matchedEmployees) {
            employeeToSearchStrings.putIfAbsent(employee.getId(), ConcurrentHashMap.newKeySet());
            employeeToSearchStrings.get(employee.getId()).add(searchString);
        }
        return matchedEmployees;
    }

    /**
     * Gets the employee with the specified id.
     *
     * @param id the employee id.
     * @return the {@link EmployeeResponse} object for the id.
     */
    @Cacheable(value = CacheNames.EMPLOYEE_BY_ID, key = "#id")
    public EmployeeResponse getEmployeeById(String id) {

        return this.employeeClient.getEmployeeById(id);
    }

    /**
     * Returns the highest salary of employees.
     *
     * @return the highest employee salary, or 0 if no employee entries are present.
     */
    @Cacheable(CacheNames.TOP_SALARY)
    public Integer getHighestSalaryOfEmployees() {
        List<EmployeeResponse> employees = this.getAllEmployees();
        return employees.stream()
                .map(EmployeeResponse::getSalary)
                .max(Integer::compareTo)
                .orElse(NO_SALARY);
    }

    /**
     * Queries the top ten earning employees, and returns their names.
     *
     * @return a collection of the top ten earning employee's names
     */
    @Cacheable(CacheNames.TOP_EARNING_EMPLOYEES)
    public List<String> getTopTenHighestEarningEmployeeNames() {

        List<EmployeeResponse> employees = this.getAllEmployees();
        return employees.stream()
                .sorted(Comparator.comparingInt(EmployeeResponse::getSalary).reversed())
                .map(EmployeeResponse::getName)
                .limit(TEN)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new employee, and adds it to the cache via the id of the {@link EmployeeResponse}. Invalidates salary-based queries as the new employee could be present for the new results.
     *
     * @param employeeInput The employee object to create
     * @return a {@link EmployeeResponse}.
     */
    @Caching(
            put =
                    @CachePut(
                            value = {CacheNames.EMPLOYEE_BY_ID},
                            key = "#result.id"),
            evict =
                    @CacheEvict(
                            value = {
                                CacheNames.EMPLOYEES,
                                CacheNames.TOP_SALARY,
                                CacheNames.TOP_EARNING_EMPLOYEES,
                                CacheNames.EMPLOYEES_BY_NAME_SEARCH
                            },
                            allEntries = true))
    public EmployeeResponse createEmployee(CreateEmployeeRequest employeeInput) {
        return employeeClient.createEmployee(employeeInput);
    }

    /**
     * Deletes the employee specified by the provided id.
     * <p>
     * Invalidates {@link CacheNames#EMPLOYEE_BY_ID}, {@link CacheNames#EMPLOYEES}, {@link CacheNames#TOP_SALARY}, {@link CacheNames#TOP_EARNING_EMPLOYEES},
     * and specific {@link CacheNames#EMPLOYEES_BY_NAME_SEARCH} entries using {@link EmployeeService#employeeToSearchStrings} & {@link EmployeeSearchCacheEvictionService}.
     * </p>
     *
     * @param id The id for the employee object to delete
     * @return true if delete was successful, false otherwise
     */
    @Caching(
            evict = {
                @CacheEvict(
                        value = {CacheNames.EMPLOYEE_BY_ID},
                        key = "#id"),
                @CacheEvict(
                        value = {
                            CacheNames.EMPLOYEES,
                            CacheNames.TOP_SALARY,
                            CacheNames.TOP_EARNING_EMPLOYEES,
                        },
                        allEntries = true)
            })
    public boolean deleteEmployeeById(String id) {
        final var deleted = employeeClient.deleteEmployeeById(id);
        if (deleted) {
            // Evict specific cached search entries attached to the employee being deleted
            final UUID employeeUuid = UUID.fromString(id);
            final Set<String> searchStringsToEvict =
                    employeeToSearchStrings.getOrDefault(employeeUuid, Collections.emptySet());
            employeeSearchCacheEvictionService.evictEmployeeFragmentsByUUID(searchStringsToEvict);
        }
        return deleted;
    }
}
