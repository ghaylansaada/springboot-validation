package io.ghaylan.springboot.validation.exceptions

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata

/**
 * Thrown when a [ConstraintMetadata] definition is invalid (e.g., a referenced property does not exist,
 * or range bounds are logically inconsistent).
 *
 * @param constraint The constraint instance that is misconfigured.
 */
class InvalidConstraintDefinitionException(
	message: String,
	val constraint: ConstraintMetadata,
	cause: Throwable? = null
): RuntimeException("${constraint.javaClass.name} error: $message", cause)