package com.reliaquest.api.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * EmployeeResponse represents the response payload for employee data.
 * <p>
 * Contains employee details:
 * <ul>
 *     <li><b>id</b>: Unique identifier (UUID)</li>
 *     <li><b>name</b>: Employee's name</li>
 *     <li><b>salary</b>: Employee's salary</li>
 *     <li><b>age</b>: Employee's age</li>
 *     <li><b>title</b>: Employee's job title</li>
 *     <li><b>email</b>: Employee's email address</li>
 * </ul>
 * <p>
 * Uses a custom JSON naming strategy to prefix non-id fields with "employee_".
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(EmployeeResponse.PrefixNamingStrategy.class)
public class EmployeeResponse {

    public static EmployeeResponse BLANK = new EmployeeResponse();

    private UUID id;
    private String name;
    private int salary;
    private int age;
    private String title;
    private String email;

    static class PrefixNamingStrategy extends PropertyNamingStrategies.NamingBase {

        @Override
        public String translate(String propertyName) {
            if ("id".equals(propertyName)) {
                return propertyName;
            }
            return "employee_" + propertyName;
        }
    }
}
