package com.reliaquest.api.service;

import com.reliaquest.api.config.ClientConfig;
import com.reliaquest.api.model.request.CreateEmployeeRequest;
import com.reliaquest.api.model.request.DeleteEmployeeRequest;
import com.reliaquest.api.model.response.DeleteEmployeeResponseWrapper;
import com.reliaquest.api.model.response.EmployeeResponse;
import com.reliaquest.api.model.response.EmployeeResponseWrapper;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/**
 * EmployeeClient is a wrapper for interfacing with a third-party Employee API.
 * <p>
 * Provides methods to:
 * <ul>
 *     <li>Retrieve all employees via {@link #getAllEmployees()}</li>
 *     <li>Get an employee by ID via {@link #getEmployeeById(String)}</li>
 *     <li>Create a new employee via {@link #createEmployee(CreateEmployeeRequest)}</li>
 *     <li>Delete an employee by ID via {@link #deleteEmployeeById(String)}</li>
 * </ul>
 * <p>
 * Uses {@link RestTemplate} for HTTP requests and handles response mapping and error logging.
 * Wraps API Responses in {@link EmployeeResponseWrapper} for POST, and GET requests, {@link DeleteEmployeeResponseWrapper} for DELETE requests.
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class EmployeeClient {

    private static final int FIRST = 0;
    private static final String GET_EMPLOYEE_BY_ID = "/{id}";

    private final RestTemplateBuilder builder;
    private final ClientConfig clientConfig;

    private RestTemplate restClient;

    @PostConstruct
    public void setup() {
        restClient = builder.build();
    }

    /**
     * Gets all the employees
     *
     * @return a list containing {@link EmployeeResponse}
     */
    public List<EmployeeResponse> getAllEmployees() {

        try {
            final var apiResponse =
                    restClient.exchange(clientConfig.getBaseUrl(), HttpMethod.GET, null, EmployeeResponseWrapper.class);
            final var responseBody = apiResponse.getBody();
            if (responseBody == null) {
                log.warn("Missing request body");
                return Collections.emptyList();
            }

            return responseBody.data();
        } catch (final RestClientResponseException exception) {
            log.error(exception.getMessage(), exception);
            return Collections.emptyList();
        }
    }

    /**
     * Gets an employee by their id
     *
     * @param id The employee id to fetch with.
     * @return the mapped {@link EmployeeResponse}
     */
    public EmployeeResponse getEmployeeById(final String id) {

        try {

            final var apiResponse = restClient.exchange(
                    clientConfig.getBaseUrl() + GET_EMPLOYEE_BY_ID,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    EmployeeResponseWrapper.class,
                    id);
            final var responseBody = apiResponse.getBody();

            if (responseBody == null) {
                return EmployeeResponse.BLANK;
            }

            return responseBody.data().get(FIRST);
        } catch (final RestClientResponseException exception) {
            log.error(exception.getMessage(), exception);
            return EmployeeResponse.BLANK;
        }
    }

    /**
     * Creates a new employee
     *
     * @param employeeInput The employee fields to create a new employee object with.
     * @return the {@link EmployeeResponse} if successful, otherwise default to {@link EmployeeResponse#BLANK}
     */
    public EmployeeResponse createEmployee(CreateEmployeeRequest employeeInput) {

        try {

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            final HttpEntity<CreateEmployeeRequest> createEntity = new HttpEntity<>(employeeInput, headers);
            final var apiResponse = restClient.exchange(
                    clientConfig.getBaseUrl(), HttpMethod.POST, createEntity, EmployeeResponseWrapper.class);

            final var responseBody = apiResponse.getBody();
            if (responseBody == null) {
                return EmployeeResponse.BLANK;
            }

            return responseBody.data().get(FIRST);
        } catch (final RestClientResponseException exception) {
            log.error(exception.getMessage(), exception);
            return EmployeeResponse.BLANK;
        }
    }

    /**
     * Deletes the employee specified by the provided id
     *
     * @param employeeId The id for the employee object to delete
     * @return true if delete was successful, false otherwise
     */
    public boolean deleteEmployeeById(final String employeeId) {

        try {

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            final DeleteEmployeeRequest deleteEmployeeRequest = new DeleteEmployeeRequest(employeeId);
            final HttpEntity<DeleteEmployeeRequest> deleteEntity = new HttpEntity<>(deleteEmployeeRequest, headers);

            final var apiResponse = restClient.exchange(
                    clientConfig.getBaseUrl(), HttpMethod.DELETE, deleteEntity, DeleteEmployeeResponseWrapper.class);
            final var responseBody = apiResponse.getBody();
            if (responseBody == null) {
                return false;
            }
            return responseBody.data();

        } catch (final RestClientResponseException exception) {
            log.error(exception.getMessage(), exception);
            return false;
        }
    }
}
