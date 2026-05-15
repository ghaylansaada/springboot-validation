package io.ghaylan.springboot.validation.constraints

import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode

/**
 * Base class for all constraint validators.
 *
 * Handles group filtering and error enrichment (path, location). Subclasses implement
 * [validate] to perform the actual constraint check.
 *
 * @param Value The type of value being validated.
 * @param Constraint The [ConstraintMetadata] type this validator operates on.
 */
abstract class ConstraintValidator<Value, Constraint: ConstraintMetadata> {

    /**
     * Public entry point: checks group applicability, delegates to [validate], and enriches
     * the returned [ApiError] with path and location from [context].
     *
     * @return An [ApiError] if validation fails, or `null` if valid or skipped.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun runValidation(
        value: Any?,
        constraint: ConstraintMetadata,
        context: ValidationContext
    ): ApiError? {
        if (!shouldValidate(constraint = constraint as Constraint, context = context)) return null

        val error = validate(
            value = value as Value?,
            constraint = constraint,
            context = context
        ) ?: return null

        return error.copy(
            path = context.fieldPath,
            location = context.location,
            message = constraint.message.ifBlank { error.message })
    }

    /** Returns true if the constraint's groups intersect with the context's active groups. */
    private fun shouldValidate(
        constraint : Constraint,
        context : ValidationContext
    ): Boolean {
        // Fast exit: if constraint has no groups, always validate
        if (constraint.groups.isEmpty()) return true

        // Fast exit: if context has no groups, no match possible
        if (context.groups.isEmpty()) return false

        // Smallest set iteration for performance
        val (small, large) = if (constraint.groups.size <= context.groups.size) {
            constraint.groups to context.groups
        }
        else context.groups to constraint.groups

        // Check for intersection
        for (validationGroup in small) {
            if (validationGroup in large) return true
        }

        return false
    }

    /**
     * Performs the actual constraint check. Return an [ApiError] (with code and optional message)
     * on failure, or `null` if valid. The framework fills in path and location automatically.
     */
    protected abstract suspend fun validate(
        value: Value?,
        constraint: Constraint,
        context: ValidationContext,
    ) : ApiError?
	
	/** Retrieves a sibling property value by [name] from the context's container object. */
    protected fun getPropertyValue(
        name : String,
        context : ValidationContext
	): Any? {
        return context.containerObject
            ?.schema[name]
            ?.accessor
            ?.getFromAny(context.containerObject.value)
    }
	
	/** Returns the [ApiErrorCode]s this validator can produce. */
    abstract fun applicableErrorCodes(): Array<ApiErrorCode>
}