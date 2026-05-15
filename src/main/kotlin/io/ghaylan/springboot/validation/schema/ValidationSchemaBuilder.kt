package io.ghaylan.springboot.validation.schema

import io.ghaylan.springboot.validation.accessor.AccessorRegistry
import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.ConstraintConverter.convertToMetadata
import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.constraints.validators.array.distinct.DistinctConstraint
import io.ghaylan.springboot.validation.constraints.validators.required.RequiredConstraint
import io.ghaylan.springboot.validation.ext.*
import io.ghaylan.springboot.validation.integration.ValidateInput
import io.ghaylan.springboot.validation.schema.RequestInputSchema.PropertySpec
import io.ghaylan.springboot.validation.schema.RequestInputSchema.ValidationConfig
import io.ghaylan.springboot.validation.schema.ValidationSchemaBuilder.generateSchemaForType
import io.ghaylan.springboot.validation.schema.ValidationSchemaBuilder.generateStaticSchemas
import io.ghaylan.springboot.validation.utils.ReflectionUtils
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import io.ghaylan.springboot.validation.utils.ValidatedMethodFinder
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import kotlin.reflect.KClass

/**
 * Generates validation schemas by analyzing controller methods and DTO structures.
 *
 * Two modes:
 * - **Static** ([generateStaticSchemas]): scans `@ValidateInput` methods at startup.
 * - **Dynamic** ([generateSchemaForType]): generates schemas on demand for arbitrary classes.
 *
 * Handles recursive fields, cycle detection, constraint-to-validator matching, and accessor resolution.
 */
object ValidationSchemaBuilder
{
    private val wildcardType = TypeInfo(Any::class, Any::class, ReflectionUtils.TypeKind.ANY)

    private val primitiveBoxedMap = mapOf(
        Int::class to Integer::class,
        Long::class to java.lang.Long::class,
        Double::class to java.lang.Double::class,
        Float::class to java.lang.Float::class,
        Short::class to java.lang.Short::class,
        Byte::class to java.lang.Byte::class,
        Char::class to Character::class,
        Boolean::class to java.lang.Boolean::class)


    /**
     * Scans all Spring MVC endpoints annotated with [ValidateInput] and generates corresponding
     * static validation schemas for request inputs.
     *
     * This should be called once at **application startup** and the result should be cached.
     * The schemas are then reused during request validation to avoid reflection at runtime.
     *
     * @param appContext The Spring application context for discovering controller beans and their methods.
     * @param allValidators A map of registered constraint validators, grouped by constraint type and input type compatibility.
     *
     * @return A list of [RequestInputSchema] instances—one per endpoint—that describe how each request should be validated.
     */
    fun generateStaticSchemas(
        appContext : ApplicationContext,
        allValidators : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>
    ) : Map<String, RequestInputSchema>
    {
        val result = mutableMapOf<String, RequestInputSchema>()

        ValidatedMethodFinder.find(appContext).forEach { (method, annotation) ->

            val validation = getValidationConfig(annotation)

            val parameters = method.parameters

            val requestBody = buildRequestBodySchema(
                method = method,
                parameters = parameters,
                allValidators = allValidators)

            val requestId = method.getUniqueIdentifier()

            result[requestId] = RequestInputSchema(
                id = requestId,
                headers = buildNonRequestBodySchema(
                    annotationClass = RequestHeader::class,
                    parameters = parameters,
                    allValidators = allValidators
                ) { it.requestHeaderName() },
                queryParams = buildNonRequestBodySchema(
                    annotationClass = RequestParam::class,
                    parameters = parameters,
                    allValidators = allValidators
                ) { it.requestParamName() },
                pathVariables = buildNonRequestBodySchema(
                    annotationClass = PathVariable::class,
                    parameters = parameters,
                    allValidators = allValidators
                ) { it.pathVariableName() },
                requestBody = requestBody?.second ?: emptyMap(),
                requestBodyTypeInfo = requestBody?.first,
                validationConfig = validation)
        }

        return result
    }


    /**
     * Dynamically generates a validation schema for a given class at runtime.
     *
     * Unlike [generateStaticSchemas], this method works **outside of Spring MVC annotations** and is
     * used for on-demand validation of arbitrary objects (e.g., uploaded DTOs or programmatically
     * constructed models).
     *
     * Supports recursive field scanning and nested property validation.
     *
     * @param rootClass The root class (usually a DTO) to analyze.
     * @param allValidators A registry of all available validators.
     *
     * @return A [Pair] containing:
     *   - The resolved [TypeInfo] of the class.
     *   - A map of field names to their validation metadata ([PropertySpec]).
     *   Returns `null` if the class is not suitable for validation (non-object-like or empty).
     */
    fun generateSchemaForType(
        rootClass : Class<*>,
        allValidators : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>
    ) : Pair<TypeInfo, Map<String, PropertySpec>>?
    {
        val type = ReflectionUtils.infoFromClass(rootClass)

        return type to (buildClassFields(
            clazz = type.resolveType.java,
            allValidators = allValidators
        ) ?: return null)
    }


    /** Converts a [ValidateInput] annotation into its [ValidationConfig] counterpart. */
    private fun getValidationConfig(annotation : ValidateInput) : ValidationConfig
    {
        return ValidationConfig(
            validateBody = annotation.validateBody,
            validateQuery = annotation.validateQuery,
            validateHeaders = annotation.validateHeaders,
            validatePathVariables = annotation.validatePath,
            singleErrorPerField = annotation.singleErrorPerField,
            groups = annotation.groups.toSet())
    }


    /**
     * Builds [PropertySpec] entries for flat request sections (headers, query params, path variables).
     * Filters parameters by [annotationClass] and resolves each name via [nameResolver].
     */
    private fun buildNonRequestBodySchema(
        annotationClass : KClass<out Annotation>,
        parameters : Array<Parameter>,
        allValidators : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>,
        nameResolver : (Parameter) -> String
    ) : Map<String, PropertySpec>
    {
        return parameters
            .filter { it.isAnnotationPresent(annotationClass.java) }
            .associate { param ->
                val type = ReflectionUtils.infoFromParameter(param)
                val resolvedName = nameResolver.invoke(param)
                resolvedName to PropertySpec(
                    realName = param.name,
                    resolvedName = resolvedName,
                    typeInfo = type,
                    constraints = filterConstraints(
                        valueType = type,
                        annotations = param.annotations,
                        allValidators = allValidators),
                    nested = emptyMap(),
                    accessor = AccessorRegistry.getOrCreate(Map::class.java, fieldRealName = param.name, fieldResolvedName = resolvedName))
            }
    }


    /**
     * Resolves the `@RequestBody` parameter and returns its [TypeInfo] paired with field schemas,
     * or `null` if no `@RequestBody` parameter is present.
     *
     * @throws IllegalStateException if more than one `@RequestBody` parameter is found.
     */
    private fun buildRequestBodySchema(
        method : Method,
        parameters : Array<Parameter>,
        allValidators : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>
    ) : Pair<TypeInfo, Map<String, PropertySpec>>?
    {
        if (parameters.count { it.isAnnotationPresent(RequestBody::class.java) } > 1) {
            error("Multiple @RequestBody parameters found in method ${method.name}. Only one is allowed.")
        }

        val requestBody = parameters.find {
            it.isAnnotationPresent(RequestBody::class.java)
        } ?: return null

        val type = ReflectionUtils.infoFromParameter(requestBody)

        return type to (buildClassFields(
            clazz = type.resolveType.java,
            allValidators = allValidators
        ) ?: return null)
    }


    /**
     * Recursively inspects [clazz] and produces a [PropertySpec] for each non-synthetic, non-static,
     * non-transient field. Cycle detection via [visited] prevents infinite recursion on self-referential graphs.
     *
     * @param parentConstraints Container-level constraints (e.g., `@Distinct`) propagated to child fields.
     * @return Field-name-to-spec map, or `null` when [clazz] is non-object-like or already visited.
     */
    private fun buildClassFields(
        clazz: Class<*>,
        parentConstraints : Map<ConstraintMetadata, ConstraintValidator<*, *>> = emptyMap(),
        allValidators : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>,
        visited: MutableSet<Class<*>> = mutableSetOf()
    ) : Map<String, PropertySpec>?
    {
        if (!visited.add(clazz)) return null // Already visited, stop recursion.
        if (!ReflectionUtils.isObjectLike(clazz)) return null

        return ReflectionUtils.getFields(clazz)
            .asSequence()
            .filterNot { it.isSynthetic }
            .filterNot { Modifier.isStatic(it.modifiers) }
            .filterNot { Modifier.isTransient(it.modifiers) }
            .associate { field ->

                val resolvedName = field.bodyFieldName()

                val type = ReflectionUtils.infoFromField(field)

                val constraints = filterConstraints(
                    valueType = type,
                    annotations = field.annotations,
                    allValidators = allValidators)

                // For array/list/map fields, pass their constraints down so element-level validators
                // (e.g., @Distinct) can access the parent container's schema.
                val nested = buildClassFields(
                    clazz = type.resolveType.java,
                    parentConstraints = if (type.isArrayOfArrays || type.isArrayOfObjects || type.isArrayOfMaps) constraints else emptyMap(),
                    allValidators = allValidators,
                    visited = visited
                ) ?: emptyMap()

                val localParentConstraints = parentConstraints.filter {
                    val constraint = it.key
                    constraint is DistinctConstraint && constraint.by.contains(resolvedName)
                }

                resolvedName to PropertySpec(
                    realName = field.name,
                    resolvedName = resolvedName,
                    typeInfo = type,
                    constraints = localParentConstraints + constraints,
                    accessor = AccessorRegistry.getOrCreate(clazz, fieldRealName = field.name, fieldResolvedName = resolvedName),
                    nested = nested)
            }
    }


    /**
     * Resolves constraint annotations into metadata + validator pairs, ordered with `@Required` first.
     */
    private fun filterConstraints(
        valueType : TypeInfo,
        annotations: Array<Annotation>,
        allValidators : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>
    ) : Map<ConstraintMetadata, ConstraintValidator<*, *>>
    {
        return annotations.asSequence()
            .mapNotNull {

                if (!it.annotationClass.java.isAnnotationPresent(Constraint::class.java)) return@mapNotNull null

                val constraint = it.convertToMetadata()

                val validator = getValidator(valueType, constraint, allValidators)
                constraint to validator
            }
            .sortedBy {
                // Prioritize required constraints first
                if (it.first is RequiredConstraint) 0 else 1
            }
            .toMap(LinkedHashMap()) // 🔒 Preserve the order
    }


    /**
     * Selects the most compatible [ConstraintValidator] for the given value type and constraint metadata.
     *
     * @param valueType The resolved input type.
     * @param constraint The constraint metadata to match against.
     * @param allValidators Map of registered validators.
     * @return The selected validator.
     *
     * @throws IllegalStateException if no compatible validator is found.
     */
    private fun getValidator(
        valueType: TypeInfo,
        constraint: ConstraintMetadata,
        allValidators: Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*, *>>>
    ) : ConstraintValidator<*, *>
    {
        val constraintValidators = allValidators[constraint::class]
            ?: error("No validator found for constraint ${constraint::class.simpleName}")

        return constraintValidators.entries.firstOrNull { (validatorType, _) ->
            isValidatorCompatible(valueType, validatorType)
        }?.value ?: error("No validator found for constraint ${constraint::class.simpleName} and value type ${valueType.concreteType.java.name}")
    }


    /**
     * Checks if a validator’s type signature is compatible with the value’s type.
     *
     * Evaluated in order: exact match, primitive/boxed, numeric, Comparable, Any wildcard,
     * supertype, wildcard map, array element match, wildcard collection.
     */
    private fun isValidatorCompatible(
        value: TypeInfo,
        validator: TypeInfo
    ): Boolean
    {
         // 1️⃣ Exact match with type arguments
         if (value.concreteType == validator.concreteType &&
             typeArgsMatch(value.typeArguments, validator.typeArguments)
         ) return true

         // 2️⃣ Primitive ↔ boxed
         if (primitiveOrBoxedMatch(value.concreteType, validator.concreteType) &&
             typeArgsMatch(value.typeArguments, validator.typeArguments)
         ) return true

         // 3️⃣ Number compatibility
         if (isNumericType(value.concreteType) && validator.concreteType == Number::class) return true
         if (value.concreteType == Number::class && isNumericType(validator.concreteType)) return true

         // 4️⃣ Comparable numeric
         if (isComparableNumeric(value.concreteType) && validator.concreteType == Comparable::class) return true

         // 5️⃣ Validator is Any
         if (validator.concreteType == Any::class) return true

         // 6️⃣ Supertype match
         if (validator.concreteType.java.isAssignableFrom(value.concreteType.java) &&
             typeArgsMatch(value.typeArguments, validator.typeArguments)
         ) return true

         // 7️⃣ Wildcard map match
         if (isMapLike(value) && isMapLike(validator) && validator.typeArguments.all { it.isWildcard() }) return true

         // 8️⃣ Array match (recursive)
         if (value.isArray && validator.isArray)
         {
             val ve = value.arrayElemType
             val va = validator.arrayElemType
             if (va == Any::class) return true
             if (ve != null && va != null)
             {
                 if (primitiveOrBoxedMatch(ve, va)) return true
                 if (isNumericType(ve) && va == Number::class) return true
             }
             return false
         }

         // 9️⃣ Collection wildcard match
         if (isCollectionLike(value) && isCollectionLike(validator) && validator.typeArguments.all { it.isWildcard() }) return true

         return false
    }


    private fun primitiveOrBoxedMatch(a: KClass<*>, b: KClass<*>): Boolean {
        return a == b || primitiveBoxedMap[a] == b || primitiveBoxedMap[b] == a
    }


    // Check if a type is numeric (primitive or boxed)
    private fun isNumericType(type: KClass<*>): Boolean
    {
        return when (type)
        {
            // Kotlin primitives
            Int::class,
            Long::class,
            Double::class,
            Float::class,
            Short::class,
            Byte::class,
            // Java boxed types
            Integer::class,
            java.lang.Long::class,
            java.lang.Double::class,
            java.lang.Float::class,
            java.lang.Short::class,
            java.lang.Byte::class,
            // Number supertype
            Number::class,
            // BigDecimal/BigInteger
            java.math.BigDecimal::class,
            java.math.BigInteger::class -> true
            else -> false
        }
    }


    // Check if type is numeric and implements Comparable
    private fun isComparableNumeric(type: KClass<*>): Boolean {
        return isNumericType(type) && Comparable::class.java.isAssignableFrom(type.java)
    }


    // Helper methods - unchanged
    private fun TypeInfo.isWildcard(): Boolean {
        return this.concreteType == Any::class || this == wildcardType
    }


    private fun isCollectionLike(type: TypeInfo): Boolean {
        return Collection::class.java.isAssignableFrom(type.concreteType.java)
    }


    private fun isMapLike(type: TypeInfo): Boolean {
        return Map::class.java.isAssignableFrom(type.concreteType.java)
    }


    private fun typeArgsMatch(actual: List<TypeInfo>, expected: List<TypeInfo>): Boolean {
        if (expected.isEmpty()) return true
        if (actual.isEmpty() && expected.all { it.isWildcard() }) return true
        if (actual.size != expected.size) return false

        return actual.zip(expected).all { (v, e) ->
            e.isWildcard() || (v.concreteType == e.concreteType && typeArgsMatch(v.typeArguments, e.typeArguments))
        }
    }
}