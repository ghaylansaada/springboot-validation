package io.ghaylan.springboot.validation.constraints.validators.comparison.lessthan

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode

object LessThanValidator : ConstraintValidator<Comparable<*>, LessThanConstraint>()
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
            context = context
        ) ?: return null

        if (value::class != otherValue::class)
        {
            return ApiError(code = ApiErrorCode.DEPENDENCY_TYPE_VIOLATION, message = "Must be the same type as `${constraint.property}`")
        }

        @Suppress("UNCHECKED_CAST")
        val comparison = (value as Comparable<Any>).compareTo(otherValue)

        if (comparison > 0)
        {
            val comparisonSymbol = if (constraint.inclusive) "less than or equal to" else "strictly less than"
            return ApiError(code = ApiErrorCode.LESS_THAN_VIOLATION, message = "Must be $comparisonSymbol `${constraint.property}`")
        }

        if (comparison == 0 && !constraint.inclusive)
        {
            return ApiError(code = ApiErrorCode.LESS_THAN_VIOLATION, message = "Must be strictly less than `${constraint.property}`")
        }

        return null
    }
}