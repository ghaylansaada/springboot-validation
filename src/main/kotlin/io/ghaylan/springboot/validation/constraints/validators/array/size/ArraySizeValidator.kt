package io.ghaylan.springboot.validation.constraints.validators.array.size

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode


object ArraySizeValidator : ConstraintValidator<Any, ArraySizeConstraint>()
{

    override suspend fun validate(
        value: Any?,
        constraint: ArraySizeConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        val size = when(value)
        {
            is Array<*> -> value.size
            is Collection<*> -> value.size
            is CharArray -> value.size
            is ByteArray -> value.size
            is ShortArray -> value.size
            is IntArray -> value.size
            is LongArray -> value.size
            is FloatArray -> value.size
            is DoubleArray -> value.size
            is BooleanArray -> value.size
            else -> return null
        }

        if (size in constraint.min..constraint.max) return null

        return ApiError(code = ApiErrorCode.ARRAY_SIZE_VIOLATION, message = "Array must contain between ${constraint.min} and ${constraint.max} elements")
    }
}