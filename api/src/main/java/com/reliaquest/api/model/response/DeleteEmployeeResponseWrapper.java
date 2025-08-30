package com.reliaquest.api.model.response;

/**
 * DeleteEmployeeResponseWrapper represents the response payload for a delete employee operation.
 * <p>
 * Contains:
 * <ul>
 *     <li><b>data</b>: Indicates if the deletion was successful</li>
 *     <li><b>status</b>: Status message of the operation</li>
 * </ul>
 * <p>
 */
public record DeleteEmployeeResponseWrapper(boolean data, String status) {}
