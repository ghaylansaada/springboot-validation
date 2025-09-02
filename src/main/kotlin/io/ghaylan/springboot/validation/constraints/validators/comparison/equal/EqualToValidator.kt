package io.ghaylan.springboot.validation.constraints.validators.comparison.equal

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.constraints.validators.comparison.lessthan.LessThanConstraint
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode


object EqualToValidator : ConstraintValidator<Comparable<*>, LessThanConstraint>()
{

    override suspend fun validate(
        value: Comparable<*>?,
        constraint: LessThanConstraint,
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

            if (comparison == 0) return null
        }

        return ApiError(code = ApiErrorCode.EQUALITY_VIOLATION, message = "Must be equal to `${constraint.property}`")
    }
}