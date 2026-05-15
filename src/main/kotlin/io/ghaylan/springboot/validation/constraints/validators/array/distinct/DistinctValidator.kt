package io.ghaylan.springboot.validation.constraints.validators.array.distinct

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.constraints.annotations.Distinct.DistinctMode
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode

/**
 * Enforces the `@Distinct` constraint by detecting duplicates in a collection via the parent
 * [ValidationContext.array] context. Dispatches to scalar, map, or object strategies based on array element type.
 */
object DistinctValidator : ConstraintValidator<Any, DistinctConstraint>()
{

    override suspend fun validate(
        value: Any?,
        constraint: DistinctConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        value ?: return null

        // The constraint operates on the containing array, not individual elements; skip when iterating.
        if (context.type?.isArray == true) return null

        val arrayValue = context.array?.value?.takeUnless {
            it.isEmpty()
        } ?: return null

        return when {
            context.array.type.isArrayOfScalars -> validateScalars(arrayValue)
            context.array.type.isArrayOfMaps && constraint.by.isNotEmpty() -> validateMaps(arrayValue, constraint)
            context.array.type.isArrayOfObjects -> validateObjects(arrayValue, constraint, context)
            else -> null
        }
    }


    private fun validateScalars(array: List<Any?>): ApiError?
    {
        return if (array.size != array.toSet().size)
        {
            ApiError(code = ApiErrorCode.DISTINCT_VALUE_VIOLATION, message = "Must be distinct within the array")
        }
        else null
    }


    private fun validateMaps(
        array: List<Any?>,
        constraint: DistinctConstraint
    ): ApiError?
    {
        return if (constraint.mode == DistinctMode.PER_FIELD)
        {
            for (key in constraint.by)
            {
                val seen = mutableSetOf<Any?>()

                for (item in array)
                {
                    val value = (item as? Map<*,*>?)?.get(key)

                    if (!seen.add(value)) {
                        return ApiError(code = ApiErrorCode.DISTINCT_VALUE_VIOLATION, message = "Must be distinct by '$key' within the array")
                    }
                }
            }
            null
        }
        else
        {
            val seen = mutableSetOf<List<Any?>>()

            for (item in array)
            {
                val combo = constraint.by.map { (item as? Map<*,*>?)?.get(it) }

                if (!seen.add(combo)) {
                    return ApiError(code = ApiErrorCode.DISTINCT_VALUE_VIOLATION, message = "Must be distinct within the array by combination of: ${constraint.by.joinToString(", ")}")
                }
            }
            null
        }
    }


    private fun validateObjects(
        array: List<Any?>,
        constraint: DistinctConstraint,
        context: ValidationContext
    ) : ApiError?
    {
        val accessors = constraint.by
            .ifEmpty { setOf(context.fieldName) }
            .mapNotNull { field ->
                field to (context.array?.schema?.get(field)?.accessor ?: return@mapNotNull null)
            }

        return if (constraint.mode == DistinctMode.PER_FIELD)
        {
            for ((field, accessor) in accessors)
            {
                val seen = mutableSetOf<Any?>()

                for (item in array)
                {
                    val value = accessor.getFromAny(item, strict = false)

                    if (!seen.add(value)) {
                        return ApiError(code = ApiErrorCode.DISTINCT_VALUE_VIOLATION, message = "Must be distinct by field '$field'")
                    }
                }
            }
            null
        }
        else
        {
            val seen = mutableSetOf<List<Any?>>()

            for (item in array)
            {
                val combo = accessors.map { (_, accessor) -> accessor.getFromAny(item, strict = false) }

                if (!seen.add(combo)) {
                    return ApiError(code = ApiErrorCode.DISTINCT_VALUE_VIOLATION, message = "Must be distinct within the array by combination of: ${constraint.by.joinToString(", ")}")
                }
            }
            null
        }
    }


    override fun applicableErrorCodes(): Array<ApiErrorCode> = arrayOf(ApiErrorCode.DISTINCT_VALUE_VIOLATION)
}