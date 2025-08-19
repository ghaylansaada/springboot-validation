package io.ghaylan.springboot.validation.exceptions

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata

/**
 * Exception thrown when a constraint metadata definition is invalid or improperly configured.
 *
 * This exception is typically raised during the instantiation or validation phase of a custom
 * [ConstraintMetadata], especially when the metadata refers to a non-existent property,
 * has logical inconsistencies, or fails to meet the expected structure.
 *
 * ### Example Scenarios
 * - A property name provided in the constraint does not exist in the target class.
 * - A range constraint is defined with `min > max`.
 * - The constraint is missing required metadata.
 *
 * @param message A human-readable explanation of the validation failure.
 * @param constraint The instance of [ConstraintMetadata] that caused the exception.
 * @param cause The underlying exception that triggered this error, if any.
 */
class InvalidConstraintDefinitionException(
    message: String,
    val constraint: ConstraintMetadata,
    cause: Throwable? = null
) : RuntimeException("${constraint.javaClass.name} error: $message", cause)