package com.reliaquest.api.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DeleteEmployeeRequest represents the payload for deleting an employee.
 * <p>
 * Validates the employee name is provided and not blank.
 * <ul>
 *     <li><b>name</b>: Name of the employee to delete (not blank)</li>
 * </ul>
 * <p>
 */
@Data
@AllArgsConstructor
public class DeleteEmployeeRequest {

    @NotBlank
    private String name;
}
