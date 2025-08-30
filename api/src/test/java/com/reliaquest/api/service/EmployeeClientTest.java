package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.config.ClientConfig;
import com.reliaquest.api.model.request.CreateEmployeeRequest;
import com.reliaquest.api.model.response.DeleteEmployeeResponseWrapper;
import com.reliaquest.api.model.response.EmployeeResponse;
import com.reliaquest.api.model.response.EmployeeResponseWrapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/**
 * Test cases for the {@link EmployeeClient}
 */
@SpringBootTest
class EmployeeClientTest {

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private ClientConfig clientConfig;

    @InjectMocks
    private EmployeeClient employeeClient;

    @BeforeEach
    void setup() {
        restTemplate = mock(RestTemplate.class);
        clientConfig = mock(ClientConfig.class);
        when(clientConfig.getBaseUrl()).thenReturn("http://fake-api/employees");

        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.build()).thenReturn(restTemplate);

        employeeClient = new EmployeeClient(builder, clientConfig);
        employeeClient.setup(); // manually call PostConstruct
    }

    @Test
    void testGetAllEmployeesReturnsList() {
        EmployeeResponse employee = new EmployeeResponse();
        employee.setName("John Doe");
        EmployeeResponseWrapper wrapper = new EmployeeResponseWrapper(List.of(employee), "Success");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(EmployeeResponseWrapper.class)))
                .thenReturn(new ResponseEntity<>(wrapper, HttpStatus.OK));

        List<EmployeeResponse> result = employeeClient.getAllEmployees();
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    void testGetAllEmployeesReturnsEmptyOnException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(EmployeeResponseWrapper.class)))
                .thenThrow(mock(RestClientResponseException.class));

        List<EmployeeResponse> result = employeeClient.getAllEmployees();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetEmployeeByIdReturnsEmployee() {
        String id = UUID.randomUUID().toString();
        EmployeeResponse employee = new EmployeeResponse();
        employee.setName("Alice");
        EmployeeResponseWrapper wrapper = new EmployeeResponseWrapper(List.of(employee), "Success");

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        eq(HttpEntity.EMPTY),
                        eq(EmployeeResponseWrapper.class),
                        eq(id)))
                .thenReturn(new ResponseEntity<>(wrapper, HttpStatus.OK));

        EmployeeResponse result = employeeClient.getEmployeeById(id);
        assertEquals("Alice", result.getName());
    }

    @Test
    void testGetEmployeeByIdReturnsBlankOnException() {
        String id = UUID.randomUUID().toString();
        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        eq(HttpEntity.EMPTY),
                        eq(EmployeeResponseWrapper.class),
                        eq(id)))
                .thenThrow(mock(RestClientResponseException.class));

        EmployeeResponse result = employeeClient.getEmployeeById(id);
        assertSame(EmployeeResponse.BLANK, result);
    }

    @Test
    void testCreateEmployeeReturnsCreatedEmployee() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        EmployeeResponse employee = new EmployeeResponse();
        employee.setName("Bob");
        EmployeeResponseWrapper wrapper = new EmployeeResponseWrapper(List.of(employee), "Success");

        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(EmployeeResponseWrapper.class)))
                .thenReturn(new ResponseEntity<>(wrapper, HttpStatus.CREATED));

        EmployeeResponse result = employeeClient.createEmployee(request);
        assertEquals("Bob", result.getName());

        // Verify headers
        ArgumentCaptor<HttpEntity<CreateEmployeeRequest>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate)
                .exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(EmployeeResponseWrapper.class));
        HttpHeaders headers = captor.getValue().getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
    }

    @Test
    void testCreateEmployeeReturnsBlankOnException() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(EmployeeResponseWrapper.class)))
                .thenThrow(mock(RestClientResponseException.class));

        EmployeeResponse result = employeeClient.createEmployee(request);
        assertSame(EmployeeResponse.BLANK, result);
    }

    @Test
    void testDeleteEmployeeByIdReturnsTrueOnSuccess() {
        String id = UUID.randomUUID().toString();
        DeleteEmployeeResponseWrapper wrapper = new DeleteEmployeeResponseWrapper(true, "Success");

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        eq(DeleteEmployeeResponseWrapper.class)))
                .thenReturn(new ResponseEntity<>(wrapper, HttpStatus.OK));

        boolean result = employeeClient.deleteEmployeeById(id);
        assertTrue(result);
    }

    @Test
    void testDeleteEmployeeByIdReturnsFalseOnException() {
        String id = UUID.randomUUID().toString();
        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        eq(DeleteEmployeeResponseWrapper.class)))
                .thenThrow(mock(RestClientResponseException.class));

        boolean result = employeeClient.deleteEmployeeById(id);
        assertFalse(result);
    }
}
