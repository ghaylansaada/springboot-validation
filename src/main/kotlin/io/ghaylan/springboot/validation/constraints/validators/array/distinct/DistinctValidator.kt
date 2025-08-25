package io.ghaylan.springboot.validation.constraints.validators.array.distinct

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import io.ghaylan.springboot.validation.constraints.annotations.Distinct.DistinctMode
import io.ghaylan.springboot.validation.model.errors.ApiError

/**
 * Validates the `@Distinct` constraint, ensuring all elements in a collection or array are unique.
 *
 * ### Description
 * The `DistinctValidator` enforces the `@Distinct` constraint by checking for duplicate elements in collections, arrays, or specific fields within objects. It supports scalar types, objects, and maps, with validation performed efficiently using cached `FieldAccessor`s. The validator adapts its behavior based on the presence of the `by` parameter and the `mode` setting in the `@Distinct` annotation.
 *
 * ### Supported types
 * - Collections or arrays of scalar types (e.g., `List<Int>`, `Array<String>`)
 * - Collections or arrays of objects (e.g., `List<User>`, `Array<Map<String, Any>>`)
 * - Fields within objects inside a parent collection (e.g., `email: String` in `List<User>`)
 *
 * ### Validation steps
 * - For collections or arrays, checks if the value is a collection; if not, delegates to parent context.
 * - If the collection is empty, considers it valid.
 * - For scalar collections, compares elements using `equals()` to detect duplicates.
 * - For objects or maps with specified `by` fields:
 *   - In `PER_FIELD` mode, ensures each field's values are unique across all elements.
 *   - In `COMBINATION` mode, ensures the combination of values from all specified fields is unique.
 * - Uses `FieldAccessor`s to retrieve field values without runtime reflection.
 * - Returns an error if duplicates are found, with appropriate multilingual error messages.
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

        // Skip validation for collections, as it’s handled item-by-item
        if (context.type?.isArray == true) return null

        // Skip validation if the parent array is empty
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
}