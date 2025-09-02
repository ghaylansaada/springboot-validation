package io.ghaylan.springboot.validation.constraints.validators.comparison.notequal

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode


object NotEqualToValidator : ConstraintValidator<Comparable<*>, NotEqualToConstraint>()
{

    override suspend fun validate(
        value: Comparable<*>?,
        constraint: NotEqualToConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        val otherValue = super.getPropertyValue(
            name = constraint.property,
            context = context)

        if (otherValue != null)
        {
            if (value::class != otherValue::class)
            {
                return ApiError(code = ApiErrorCode.DEPENDENCY_TYPE_VIOLATION, message = "Must be the same type as `${constraint.property}`")
            }

            @Suppress("UNCHECKED_CAST")
            val comparison = (value as Comparable<Any>).compareTo(otherValue)

            if (comparison != 0) return null
        }

        return ApiError(code = ApiErrorCode.INEQUALITY_VIOLATION, message = "Must not be equal to `${constraint.property}`")
    }
}