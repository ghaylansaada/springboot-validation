package io.ghaylan.springboot.validation.exceptions

import io.ghaylan.springboot.validation.model.errors.ApiError

/**
 * Thrown by the validation engine when one or more constraint violations are detected.
 * Catch this in a `@ControllerAdvice` to produce a structured error response.
 *
 * @property errors The list of validation failures that triggered this exception.
 */
class ConstraintViolationException(
	val errors: List<ApiError>
): RuntimeException("Validation failed for ${errors.size} field(s).")