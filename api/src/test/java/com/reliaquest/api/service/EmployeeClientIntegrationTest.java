package com.reliaquest.api.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.reliaquest.api.config.ClientConfig;
import com.reliaquest.api.model.request.CreateEmployeeRequest;
import com.reliaquest.api.model.response.EmployeeResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

/**
 * Integration Test cases for the {@link EmployeeClient}
 */
@WireMockTest(httpPort = 8080)
public class EmployeeClientIntegrationTest {

    private static final String VALID_REQ =
            """
            {
              "data": [
                {
                  "id": "0c38f567-7b4c-4e42-b1a2-c448d00a4724",
                  "employee_name": "John Doe",
                  "employee_role": "Engineer",
                  "employee_salary": 90000,
                  "employee_age": 30,
                  "employee_title": "Engineer",
                  "employee_email": "john.doe@example.com"
                }
              ]
            }
            """;
    private static final String VALID_DELETE_REQ =
            """
            {
              "data": true,
              "status": "Success"
            }
            """;

    private EmployeeClient employeeClient;

    @BeforeEach
    void setup() {
        ClientConfig config = new ClientConfig() {
            @Override
            public String getBaseUrl() {
                return "http://localhost:8080/employees";
            }
        };
        employeeClient = new EmployeeClient(new RestTemplateBuilder(), config);
        employeeClient.setup();
    }

    @Test
    void testGetAllEmployees() {
        stubFor(get(urlEqualTo("/employees")).willReturn(okJson(VALID_REQ)));

        List<EmployeeResponse> employees = employeeClient.getAllEmployees();
        assertEquals(1, employees.size());
        assertEquals("John Doe", employees.get(0).getName());
    }

    @Test
    void testGetEmployeeById() {
        stubFor(get(urlEqualTo("/employees/0c38f567-7b4c-4e42-b1a2-c448d00a4724"))
                .willReturn(okJson(VALID_REQ)));

        EmployeeResponse employee = employeeClient.getEmployeeById("0c38f567-7b4c-4e42-b1a2-c448d00a4724");
        assertEquals("John Doe", employee.getName());
    }

    @Test
    void testCreateEmployee() {
        stubFor(post(urlEqualTo("/employees")).willReturn(okJson(VALID_REQ)));

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setAge(20);
        request.setTitle("Engineer");
        request.setName("John Doe");
        request.setSalary(90_000);
        EmployeeResponse employee = employeeClient.createEmployee(request);
        assertEquals("John Doe", employee.getName());
    }

    @Test
    void testDeleteEmployeeById() {
        stubFor(delete(urlEqualTo("/employees")).willReturn(okJson(VALID_DELETE_REQ)));

        boolean deleted = employeeClient.deleteEmployeeById("0c38f567-7b4c-4e42-b1a2-c448d00a4724");
        assertTrue(deleted);
    }
}
