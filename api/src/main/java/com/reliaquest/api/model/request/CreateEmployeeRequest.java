package com.reliaquest.api.model.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CreateEmployeeRequest represents the payload for creating a new employee.
 * <p>
 * Validates required fields and value constraints:
 * <ul>
 *     <li><b>name</b>: Employee's name (not blank)</li>
 *     <li><b>salary</b>: Employee's salary (positive, not null)</li>
 *     <li><b>age</b>: Employee's age (between 16 and 75, not null)</li>
 *     <li><b>title</b>: Employee's job title (not blank)</li>
 * </ul>
 * <p>
 */
@Data
@NoArgsConstructor
public class CreateEmployeeRequest {

    @NotBlank
    private String name;

    @Positive
    @NotNull
    private Integer salary;

    @Min(16)
    @Max(75)
    @NotNull
    private Integer age;

    @NotBlank
    private String title;

}
