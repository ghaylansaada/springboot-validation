package io.ghaylan.springboot.validation.exceptions

import io.ghaylan.springboot.validation.model.errors.ApiError

/**
 * Exception thrown when input validation fails for one or more request parameters, body fields,
 * headers, or path variables.
 *
 * This exception is typically thrown automatically by the custom validation framework
 * when constraint violations are detected during request processing.
 *
 * ### Details:
 * - Contains a list of [ApiError] instances describing each validation failure.
 * - Can be caught globally by a Spring `@ControllerAdvice` to produce a structured error response.
 *
 * @property errors The list of validation errors that caused the exception.
 */
class ConstraintViolationException(
    val errors : List<ApiError>
) : RuntimeException("Validation failed for ${errors.size} field(s).")