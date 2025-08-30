package com.reliaquest.api.constants;

import com.reliaquest.api.model.request.CreateEmployeeRequest;
import com.reliaquest.api.service.EmployeeService;

/**
 * Defines cache name constants used across the application for caching employee data.
 * <p>
 * These are referenced via caching annotations
 * (e.g., {@code @Cacheable}, {@code @CacheEvict}, {@code @CachePut}) to ensure consistency in {@link EmployeeService}.
 * </p>
 *
 * <ul>
 *   <li>{@link #EMPLOYEES} –  Cache for querying all employee data.</li>
 *   <li>{@link #EMPLOYEES_BY_NAME_SEARCH} – Cache for employees retrieved by name or search fragments.</li>
 *   <li>{@link #EMPLOYEE_BY_ID} – Cache for a single employee retrieved by ID.</li>
 *   <li>{@link #TOP_SALARY} – Cache for the top salary value among employees.</li>
 *   <li>{@link #TOP_EARNING_EMPLOYEES} – Cache for the list of employees with the highest salaries.</li>
 * </ul>
 *
 * <p>
 *     {@code @CacheEvict's} occur on {@link EmployeeService#createEmployee(CreateEmployeeRequest)} and
 *     {@link EmployeeService#deleteEmployeeById(String)} to maintain cache consistency as the underlying data changes.
 * </p>
 */
public final class CacheNames {

    private CacheNames() {}

    public static final String EMPLOYEES = "employees";
    public static final String EMPLOYEES_BY_NAME_SEARCH = "employeesByNameSearch";
    public static final String EMPLOYEE_BY_ID = "employeeById";
    public static final String TOP_SALARY = "topSalary";
    public static final String TOP_EARNING_EMPLOYEES = "topEarningEmployees";
}
