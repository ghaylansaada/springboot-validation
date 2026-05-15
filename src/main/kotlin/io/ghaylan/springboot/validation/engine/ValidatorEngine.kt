package io.ghaylan.springboot.validation.engine

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.exceptions.ConstraintViolationException
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.integration.ValidationRegistry
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.ValidationContextValue
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiError.ErrorLocation
import io.ghaylan.springboot.validation.schema.RequestInputSchema.PropertySpec
import io.ghaylan.springboot.validation.utils.CollectionUtils
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeKind
import kotlin.reflect.KClass

/**
 * Core runtime engine that validates DTOs and HTTP requests against precompiled schemas.
 *
 * Supports nested objects, multi-dimensional arrays, cross-field rules, validation groups,
 * and fail-fast or comprehensive error collection. Thread-safe and stateless.
 *
 * @param validationRegistry Schema repository for resolving validation metadata.
 */
open class ValidatorEngine(val validationRegistry : ValidationRegistry) {
	
    /** Deduplication key for [ApiError]s: unique per (field, code, location). */
    data class ErrorKey(val field: String?, val code: Any?, val location: ErrorLocation?)
	

    /**
     * Validates an object or collection of type [T] against its schema (created on demand if needed).
     *
     * @param params The object to validate.
     * @param location Error location tag (default: BODY).
     * @param singleErrorPerField If true, stops at the first error per field.
     * @param groups Validation groups to apply (default: [OnDefault]).
     * @throws ConstraintViolationException if validation fails.
     */
    suspend inline fun <reified T> validate(
        params : T?,
        location : ErrorLocation = ErrorLocation.BODY,
        singleErrorPerField : Boolean = true,
        groups : Array<KClass<*>> = arrayOf(OnDefault::class)
	) {
		
        val schema = validationRegistry.resolveSchemaByClass(T::class.java)
        val errors = mutableListOf<ApiError>()

        val context = ValidationContext(
            fieldName = "",
            fieldPath = "",
            type = null,
            location = location,
            stopOnFirstError = singleErrorPerField,
            groups = groups.toSet(),
            array = null,
            containerObject = ValidationContextValue(
                value = params,
                schema = schema.second,
                type = schema.first))

        validate(
            value = params,
            type = schema.first,
            fields = schema.second,
            context = context,
            errors = errors)

        val finalErrors = deduplicateErrors(errors)
	    
	    if (finalErrors.isNotEmpty()) {
			throw ConstraintViolationException(finalErrors)
		}
    }

    /**
     * Validates all enabled sections of an HTTP request against its pre-registered schema.
     *
     * Validates body (recursive), query, headers, and path variables independently,
     * then returns deduplicated errors.
     *
     * @param id Unique method identifier for schema lookup.
     * @param body Deserialized request body.
     * @param params Query parameters.
     * @param headers HTTP headers.
     * @param pathVariables Path variables.
     * @throws IllegalStateException if no schema is registered for [id].
     */
    suspend fun validateRequest(
        id : String,
        body: Any?,
        params : Map<String, Any?>?,
        headers : Map<String, Any?>?,
        pathVariables : Map<String, Any?>?
    ) : List<ApiError> {
		
        val schema = validationRegistry.getSchemaByRequest(id) ?: error("No validation schema found for request $id.")
        val errors = mutableListOf<ApiError>()

        // Base context copied and specialized for each request section (body / query / headers / path).
        val baseCtx = ValidationContext(
            fieldName = "",
            fieldPath = "",
            type = null,
            location = ErrorLocation.BODY,
            groups = schema.validationConfig.groups.toSet(),
            stopOnFirstError = schema.validationConfig.singleErrorPerField,
            array = null,
            containerObject = null)

        // ---------------- Body ----------------
        if (schema.validationConfig.validateBody && schema.requestBody.isNotEmpty()) {
            validate(
                value = body,
                type = schema.requestBodyTypeInfo!!,
                fields = schema.requestBody,
                context = baseCtx.copy(location = ErrorLocation.BODY, type = schema.requestBodyTypeInfo),
                errors = errors)
        }

        // ---------------- Query ----------------
        if (schema.validationConfig.validateQuery && schema.queryParams.isNotEmpty()) {
            val currentValueCtx = ValidationContextValue<Any>(
                value = params,
                schema = schema.queryParams,
                type = TypeInfo(rawRootType = Map::class, concreteType = Map::class, kind = TypeKind.MAP))

            val queryCtx = baseCtx.copy(location = ErrorLocation.QUERY, containerObject = currentValueCtx)

            validateHeadersOrParamsOrPathVariables(
                params = params,
                schema = schema.queryParams,
                context = queryCtx,
                errors = errors)
        }

        // ---------------- Headers ----------------
        if (schema.validationConfig.validateHeaders && schema.headers.isNotEmpty()) {
            val currentValueCtx = ValidationContextValue<Any>(
                value = headers,
                schema = schema.headers,
                type = TypeInfo(rawRootType = Map::class, concreteType = Map::class, kind = TypeKind.MAP))

            val headerCtx = baseCtx.copy(location = ErrorLocation.HEADER, containerObject = currentValueCtx)

            validateHeadersOrParamsOrPathVariables(
                params = headers,
                schema = schema.headers,
                context = headerCtx,
                errors = errors)
        }

        // ------------- Path Variables ------------
        if (schema.validationConfig.validatePathVariables && schema.pathVariables.isNotEmpty()) {
            val currentValueCtx = ValidationContextValue<Any>(
                value = pathVariables,
                schema = schema.pathVariables,
                type = TypeInfo(rawRootType = Map::class, concreteType = Map::class, kind = TypeKind.MAP))

            val pathVariableCtx = baseCtx.copy(location = ErrorLocation.PATH, containerObject = currentValueCtx)

            validateHeadersOrParamsOrPathVariables(
                params = pathVariables,
                schema = schema.pathVariables,
                context = pathVariableCtx,
                errors = errors)
        }

        return deduplicateErrors(errors)
    }
	
    /** Deduplicates errors by (path, code, location), preserving first occurrence. */
    fun deduplicateErrors(errors: List<ApiError>) : List<ApiError> {
        if (errors.isEmpty()) return errors

        val seen = HashSet<ErrorKey>()
        val unique = ArrayList<ApiError>(errors.size)

        for (error in errors) {
            val key = ErrorKey(error.path, error.code, error.location)

            if (seen.add(key)) {
                unique.add(error)
            }
        }

        return unique
    }
	
    /**
     * Dispatches validation based on type shape: arrays go to [validateArray],
     * objects go to [validateFields], scalars are rejected.
     */
    suspend fun validate(
        value : Any?,
        type : TypeInfo,
        fields: Map<String, PropertySpec>,
        context: ValidationContext,
        errors: MutableList<ApiError>
	) {
        if (type.isArray) {
            // forceNonEmpty=true so element-level constraints fire even on empty root arrays (path "[0]").
            validateArray(
                params = value,
                type = type,
                schema = fields,
                context = context,
                errors = errors,
                // only matters at root level when the input is an array
                forceNonEmpty = true,
                // no root-level constraints in this generic validation entry point
                constraints = emptyMap())
        }
        else if (type.isObject) {
            validateFields(
                param = value,
                type = type,
                parentType = null,
                fields = fields,
                context = context,
                errors = errors)
        }
        else {
            error("Param must be an object or an array of objects to be validated.")
        }
    }

    /**
     * Recursively validates arrays/lists of any depth: scalars, objects, or nested arrays.
     * Applies container-level constraints, then recurses into each element.
     *
     * @param forceNonEmpty If true, empty/null arrays are treated as containing one null element
     *                      so that element-level constraints (e.g., `@Required`) still fire.
     */
    private suspend fun validateArray(
        params: Any?,
        type: TypeInfo,
        schema: Map<String, PropertySpec>,
        context: ValidationContext,
        errors: MutableList<ApiError>,
        forceNonEmpty: Boolean,
        constraints: Map<ConstraintMetadata, ConstraintValidator<*, *>>
	) {
        val elementType = type.typeArguments.first()

        val elements = CollectionUtils.normalizeList(params).let {
            if (forceNonEmpty) it.ifEmpty { listOf(Any()) } else it
        }

        val arrayValueCtx = ValidationContextValue(
            value = elements,
            schema = schema,
            type = type)

        suspend fun validateAsContainerIfApplicable() {
			
            if (constraints.any { it.key.appliesToContainer }) {
                validateValue(
                    value = params,
                    context = context,
                    errors = errors,
                    constraints = constraints)
            }
        }

        when {
            // ----- Case 1: Current level is an array-of-arrays (multi-dimensional) -----
            type.isArrayOfArrays -> {

                validateAsContainerIfApplicable()

                elements.forEachIndexed { idx, element ->

                    val nestedCtx = context.copy(
                        fieldPath = appendIndex(context.fieldPath, idx),
                        array = arrayValueCtx,
                        containerObject = null)

                    validateArray(
                        params = element,
                        type = elementType,
                        schema = schema,
                        context = nestedCtx,
                        errors = errors,
                        forceNonEmpty = forceNonEmpty,
                        constraints = constraints)
                }
            }

            // ----- Case 2: Current level is an array-of-objects -----
            type.isArrayOfObjects -> {

                validateAsContainerIfApplicable()

                elements.forEachIndexed { idx, element ->

                    val elemCtx = context.copy(
                        fieldPath = appendIndex(context.fieldPath, idx),
                        array = arrayValueCtx)

                    validateFields(
                        param = element,
                        type = elementType,
                        parentType = type,
                        fields = schema,
                        context = elemCtx,
                        errors = errors)
                }
            }

            // ----- Case 3: Current level is an array-of-scalars -----
            else -> {

                elements.forEachIndexed { idx, element ->

                    val elemCtx = context.copy(
                        fieldPath = appendIndex(context.fieldPath, idx),
                        type = elementType,
                        array = arrayValueCtx,
                        containerObject = null)

                    validateValue(
                        value = element,
                        context = elemCtx,
                        errors = errors,
                        constraints = constraints)
                }
            }
        }
    }

    /**
     * Validates all declared fields on an object. For each field: resolves its value,
     * applies constraints, then recurses into nested objects or arrays.
     */
    private suspend fun validateFields(
        param : Any?,
        type : TypeInfo,
        parentType : TypeInfo?,
        fields : Map<String, PropertySpec>,
        context : ValidationContext,
        errors : MutableList<ApiError>
	) {
        fields.forEach { (_, field) ->

            val value = param?.let { field.accessor.getFromAny(it, strict = false) }

            val arrayValueCtx = if (field.typeInfo.isArray) {
                ValidationContextValue(
                    value = CollectionUtils.normalizeList(value),
                    schema = field.nested,
                    type = field.typeInfo)
            }
            // Delegate array context upward so cross-element validators can reach the parent list.
            else if ((type.isObject || type.isMap) && (parentType?.isArrayOfObjects == true || parentType?.isArrayOfMaps == true)) {
                context.array
            }
            else null

            val objectValueCtx = ValidationContextValue(
                value = param,
                schema = fields,
                type = type)

            val fieldCtx = context.copy(
                fieldName = field.resolvedName,
                fieldPath = appendPath(context.fieldPath, field.resolvedName),
                type = field.typeInfo,
                containerObject = objectValueCtx,
                array = arrayValueCtx)

            validateValue(
                value = value,
                context = fieldCtx,
                errors = errors,
                constraints = field.constraints)

            if (value == null) return@forEach

            if (field.typeInfo.isArray) {
                validateArray(
                    params = value,
                    type = field.typeInfo,
                    schema = field.nested,
                    context = fieldCtx,
                    errors = errors,
                    forceNonEmpty = true,
                    constraints = field.constraints)
            }
            else if (field.typeInfo.isObject) {
                validateFields(
                    param = value,
                    type = field.typeInfo,
                    fields = field.nested,
                    parentType = null,
                    context = fieldCtx,
                    errors = errors)
            }
        }
    }
	
    /** Validates flat key-value maps (query params, headers, path variables) against their schema. */
    @Suppress("KDocUnresolvedReference")
    private suspend fun validateHeadersOrParamsOrPathVariables(
        params : Map<String, Any?>?,
        schema : Map<String, PropertySpec>,
        context: ValidationContext,
        errors: MutableList<ApiError>
	) {
        if (schema.isEmpty()) return

        schema.forEach { (_, param) ->

            val value = params?.get(param.resolvedName)
            val paramCtx = context.copy(fieldName = param.resolvedName, fieldPath = param.resolvedName)

            validateValue(
                value = value,
                context = paramCtx,
                errors = errors,
                constraints = param.constraints)

            if (param.typeInfo.isArrayOfScalars) {
                val elements = CollectionUtils.normalizeList(value)

                val parentArray = ValidationContextValue(
                    value = elements,
                    schema = emptyMap(),
                    type = param.typeInfo)

                elements.forEachIndexed { idx, elem ->

                    val elemCtx = paramCtx.copy(
                        fieldPath = appendIndex(paramCtx.fieldPath, idx),
                        array = parentArray,
                        containerObject = null)

                    validateValue(
                        value = elem,
                        context = elemCtx,
                        errors = errors,
                        constraints = param.constraints)
                }
            }
        }
    }
	
    /** Joins [base] and [child] with a dot. Handles empty base or child gracefully. */
    private fun appendPath(base: String, child: String): String {
        if (child.isEmpty()) return base
        if (base.isEmpty()) return child
        return "$base.$child"
    }

    /** Appends `[idx]` to [base]. Returns `"[idx]"` when base is empty. */
    private fun appendIndex(base: String, idx: Int): String {
        return if (base.isEmpty()) "[$idx]" else "$base[$idx]"
    }

    /** Runs all [constraints] against [value], honoring [ValidationContext.stopOnFirstError]. */
    private suspend fun validateValue(
        value: Any?,
        context: ValidationContext,
        errors: MutableList<ApiError>,
        constraints: Map<ConstraintMetadata, ConstraintValidator<*, *>>
	) {
        if (constraints.isEmpty()) return

        if (context.stopOnFirstError) {
            for (constraint in constraints) {
                val error = constraint.value.runValidation(value, constraint.key, context)

                if (error != null) {
                    errors += error
                    return
                }
            }
        }
        else {
            constraints.forEach {
                it.value.runValidation(value, it.key, context)?.let(errors::add)
            }
        }
    }
}