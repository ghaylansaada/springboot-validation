package io.ghaylan.springboot.validation.schema

import io.ghaylan.springboot.validation.utils.ReflectionUtils
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import io.ghaylan.springboot.validation.accessor.AccessorRegistry
import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.ConstraintConverter.convertToMetadata
import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.utils.ValidatedMethodFinder
import io.ghaylan.springboot.validation.constraints.validators.array.distinct.DistinctConstraint
import io.ghaylan.springboot.validation.constraints.validators.required.RequiredConstraint
import io.ghaylan.springboot.validation.ext.bodyFieldName
import io.ghaylan.springboot.validation.integration.ValidateInput
import io.ghaylan.springboot.validation.schema.RequestInputSchema.PropertySpec
import io.ghaylan.springboot.validation.schema.RequestInputSchema.ValidationConfig
import io.ghaylan.springboot.validation.ext.generateRequestId
import io.ghaylan.springboot.validation.ext.pathVariableName
import io.ghaylan.springboot.validation.ext.requestHeaderName
import io.ghaylan.springboot.validation.ext.requestParamName
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
 * `ValidationSchemaBuilder` serves as the core utility for generating validation schemas in a custom validation
 * framework tailored for Spring-based web applications. It analyzes controller methods and DTO structures to
 * create structured [RequestInputSchema] objects that encapsulate validation rules for various HTTP request inputs.
 *
 * The builder supports two primary modes of operation to accommodate different use cases:
 *
 * - **Static Schema Generation** (via [generateStaticSchemas]): Performed at application startup by scanning
 *   controller methods annotated with [@ValidateInput]. This produces precomputed, reusable schemas for efficient
 *   runtime validation without repeated reflection. Ideal for standard API endpoints where performance is critical.
 *
 * - **Dynamic Schema Generation** (via [generateSchemaForType]): Executed on-demand for arbitrary classes, such as
 *   user-uploaded DTOs, external payloads, or admin-configured data models. This enables flexible validation in
 *   scenarios where structures are not known at compile-time or startup.
 *
 * ## Business Logic Overview
 * The schema generation process is driven by the following key principles:
 * - **Input Type Support**: Validates `@RequestHeader`, `@RequestParam`, `@PathVariable`, and a single `@RequestBody`
 *   per method. Non-annotated parameters are ignored.
 * - **Deep, Recursive Validation**: Supports nested objects, collections, arrays, and maps with cycle detection to
 *   prevent infinite recursion in self-referential structures.
 * - **Constraint Resolution and Matching**: Constraints (annotated with [@Constraint]) are converted to metadata and
 *   paired with compatible [ConstraintValidator]s based on type compatibility rules (exact matches, supertypes,
 *   wildcards, and container-specific handling).
 * - **Fail-Fast Optimization**: Required constraints (e.g., [@Required]) are prioritized in ordering to short-circuit
 *   validation on missing fields.
 * - **Inheritance and Delegation**: Parent-level constraints (e.g., [@Distinct] on collections) are selectively
 *   delegated to child elements or fields when applicable (e.g., for arrays/collections of objects/maps).
 * - **Accessor Integration**: Uses [AccessorRegistry] to provide property accessors for runtime value extraction.
 * - **Type-Aware Flexibility**: Handles generics, wildcards, and container types with specialized compatibility checks.
 *
 * ## Key Features
 * - Automatic constraint prioritization for efficient validation.
 * - Support for complex types including arrays, collections, and maps with element-level validation.
 * - Built-in cycle detection using a visited set during recursive field analysis.
 * - Strict validation of request bodies (only one per method allowed).
 * - Configurable validation scopes via [@ValidateInput] (e.g., enable/disable body, query, headers, or paths).
 *
 * ## Usage Examples
 *
 * ### Static Generation (Startup-Time)
 * ```kotlin
 * val schemas = ValidationSchemaBuilder.generateStaticSchemas(applicationContext, validatorRegistry)
 * // Cache the map and retrieve schemas by request ID during incoming requests for validation
 * ```
 *
 * ### Dynamic Generation (Runtime)
 * ```kotlin
 * val (typeInfo, schema) = ValidationSchemaBuilder.generateSchemaForType(MyDynamicDto::class.java, validatorRegistry)
 *     ?: error("Invalid type for validation")
 * // Apply the schema to validate arbitrary objects, e.g., from uploads or integrations
 * ```
 *
 * ## Integration Considerations
 * - Relies on utilities like [ReflectionUtils] for type introspection, [AccessorRegistry] for property access,
 *   and [ValidatedMethodFinder] for discovering annotated methods.
 * - Validators must be registered in a map keyed by constraint metadata class and supported [TypeInfo].
 * - Constraints on container types (e.g., lists) may propagate to elements if the elements are object-like.
 * - For performance, prefer static schemas for fixed endpoints; use dynamic only for variable or external structures.
 * - Errors (e.g., multiple `@RequestBody`s) are thrown during schema generation to fail early.
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

            val requestId = method.generateRequestId()

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


    /**
     * Extracts validation settings from the given [ValidateInput] annotation.
     *
     * This builds a [ValidationConfig] based on the
     * properties defined in the annotation.
     *
     * @param annotation The [ValidateInput] annotation to convert into a validation configuration.
     * @return The extracted [ValidationConfig].
     */
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
     * Builds validation metadata for request headers, query parameters, or path variables.
     *
     * Scans method parameters annotated with the given annotation type and builds corresponding
     * [PropertySpec] for each parameter, resolving validators and accessor methods.
     *
     * @param annotationClass The Spring annotation type to filter (e.g., [RequestHeader], [RequestParam], [PathVariable]).
     * @param parameters The parameters of the controller method.
     * @param allValidators A registry of all available validators.
     * @param nameResolver A function that resolves the name of the field (e.g., from annotation or parameter name).
     *
     * @return A map of logical field names to their validation specifications ([PropertySpec]).
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
     * Builds validation specifications for the `@RequestBody` parameter of a method.
     *
     * @param method The controller method being analyzed.
     * @param parameters The list of method parameters.
     * @param allValidators The full registry of validators.
     *
     * @return A [Pair] containing:
     *   - [TypeInfo] representing the structure of the request body.
     *   - A map of field names to their corresponding [PropertySpec].
     *   Returns `null` if no `@RequestBody` is found.
     *
     * @throws IllegalStateException If more than one `@RequestBody` is found (which is not allowed).
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
     * Recursively builds validation specifications for all object-like fields in a class.
     *
     * This function inspects the fields of a class and constructs a [PropertySpec] for each field,
     * including nested fields for object-like or container types. It filters out synthetic, static,
     * or transient fields and resolves applicable constraints using the provided validator registry.
     *
     * To prevent infinite recursion in cyclic object graphs, already-visited classes are tracked.
     *
     * Special handling:
     * - For array or collection types of objects or maps, constraints can be delegated to their elements.
     * - Parent-level constraints (e.g., `DistinctConstraint`) are applied to relevant child fields.
     *
     * @param clazz The class whose fields will be inspected.
     * @param parentConstraints Constraints inherited from parent context (optional, default = empty).
     * @param allValidators Registry of all known constraint validators.
     * @param visited A set of classes already visited to prevent cycles (optional, default = empty set).
     * @return A map of field names to [PropertySpec], or `null` if the class is non-object-like or already visited.
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

                // Obtain the TypeInfo for this field
                val type = ReflectionUtils.infoFromField(field)

                // Resolve constraints declared on this field
                val constraints = filterConstraints(
                    valueType = type,
                    annotations = field.annotations,
                    allValidators = allValidators)

                // Recursively build validation specs for nested object fields.
                // If the field is an array, list, or map of objects, delegate parent constraints to its elements
                val nested = buildClassFields(
                    // The type of the current field to browse its fields
                    clazz = type.resolveType.java,
                    // If the current field is a list of non-scalar elements, delegate its constraints to its elements.
                    parentConstraints = if (type.isArrayOfArrays || type.isArrayOfObjects || type.isArrayOfMaps) constraints else emptyMap(),
                    allValidators = allValidators,
                    visited = visited
                ) ?: emptyMap()

                // Merge any parent-level constraints that apply to this field
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
     * Extracts and resolves all applicable constraint annotations for a given value type, returning a map
     * of [ConstraintMetadata] to the corresponding [ConstraintValidator] instances.
     *
     * This method performs the following steps:
     *
     * 1. **Resolves constraint annotations into metadata:**
     *    Annotations that are meta-annotated with `@Constraint` are converted into [ConstraintMetadata] via
     *    the `convertToMetadata()` extension.
     *
     * 2. **Finds the best validator for each constraint:**
     *    For each included constraint, the most compatible [ConstraintValidator] is selected based on the
     *    type of the field or parameter being validated (`valueType`). This includes:
     *    - Exact type match
     *    - Assignable supertype with matching generic arguments
     *    - Wildcard support (e.g., `Any`, wildcard collections, etc.)
     *
     * 3. **Prioritizes required constraints:**
     *    Constraints that enforce presence (such as [RequiredConstraintMetadata]) are sorted to appear
     *    first in the resulting map to support fail-fast behavior—stopping validation early if the field is missing.
     *
     * 4. **Returns a deterministic, ordered map:**
     *    A [LinkedHashMap] is used to preserve the insertion order of constraints, ensuring consistent validation behavior.
     *
     * @param valueType The [TypeInfo] representing the type of the field or method parameter being validated.
     * @param annotations The list of annotations declared on the field or parameter.
     * @param allValidators A map of available validators grouped by constraint type and supported [TypeInfo].
     *
     * @return A [LinkedHashMap] of resolved [ConstraintMetadata] to their corresponding [ConstraintValidator]s,
     *         ordered with required constraints first.
     *
     * @throws IllegalStateException If no compatible validator is found for a constraint.
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
     * Determines whether a given validator's supported type is compatible with the actual type being validated.
     *
     * Compatibility is determined using the following strategies (evaluated in order):
     *
     * 1. **Exact Match (including generic arguments):**
     *    - The concrete types must be equal.
     *    - All corresponding generic type arguments must match exactly, or be wildcards in the validator.
     *
     *    ✅ Examples:
     *      - `value = List<String>`,     validator = `List<String>` → true
     *      - `value = List<String>`,     validator = `List<*>`      → true
     *      - `value = Map<String, Int>`, validator = `Map<*, *>`    → true
     *      - `value = Array<String>`,    validator = `Array<String>` → true
     *
     *    ❌ Examples:
     *      - `value = List<String>`,     validator = `List<Int>`     → false
     *      - `value = Array<String>`,    validator = `Array<Any>`    → false
     *
     * 2. **Assignable Supertype Match (with type argument compatibility):**
     *    - The validator’s concrete type must be a supertype of the value’s.
     *    - Type arguments must match exactly or be wildcards in the validator.
     *
     *    ✅ Examples:
     *      - `value = ArrayList<String>`,          validator = `List<*>`       → true
     *      - `value = LinkedHashMap<String, Int>`, validator = `Map<*, *>`     → true
     *
     *    ❌ Examples:
     *      - `value = ArrayList<String>`, validator = `Collection<Int>` → false
     *
     * 3. **Validator Accepts Any:**
     *    - If the validator’s concrete type is `Any`, it matches any value type.
     *
     *    ✅ Examples:
     *      - `value = String`, validator = `Any` → true
     *      - `value = List<Int>`, validator = `Any` → true
     *
     * 4. **Wildcard Map Match:**
     *    - Validator must be a map type (`Map<*, *>`) with wildcard key and value types.
     *    - Value must also be a map type.
     *
     *    ✅ Example:
     *      - `value = HashMap<String, Int>`, validator = `Map<*, *>` → true
     *
     *    ❌ Example:
     *      - `value = List<String>`, validator = `Map<*, *>` → false
     *
     * 5. **Array Match:**
     *    - Both value and validator must be arrays.
     *    - If the validator’s element type is `Any`, all arrays are accepted.
     *    - If the validator’s element type is a map, arrays of maps are also accepted.
     *    - Otherwise, element types must match exactly.
     *
     *    ✅ Examples:
     *      - `value = Array<String>`, validator = `Array<Any>` → true
     *      - `value = Array<Map<String, Int>>`, validator = `Array<Map<*, *>>` → true
     *      - `value = Array<Int>`, validator = `Array<Int>` → true
     *
     *    ❌ Examples:
     *      - `value = Array<String>`, validator = `Array<Int>` → false
     *
     * 6. **Wildcard Collection Match:**
     *    - Validator must be a collection type (`List`, `Set`, etc.) with all wildcard type arguments.
     *    - Value must also be a collection type.
     *
     *    ✅ Examples:
     *      - `value = ArrayList<Int>`, validator = `List<*>` → true
     *      - `value = HashSet<String>`, validator = `Collection<*>` → true
     *
     *    ❌ Examples:
     *      - `value = Map<String, String>`, validator = `List<*>` → false
     *
     * @param value The actual [TypeInfo] of the value being validated (e.g., `List<String>`, `Array<Int>`, etc.).
     * @param validator The [TypeInfo] representing the validator's supported type signature.
     * @return `true` if the validator is compatible with the value type, `false` otherwise.
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


    private fun primitiveOrBoxedMatch(a: KClass<*>, b: KClass<*>): Boolean
    {
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
    private fun isComparableNumeric(type: KClass<*>): Boolean
    {
        return isNumericType(type) && Comparable::class.java.isAssignableFrom(type.java)
    }


    // Helper methods - unchanged
    private fun TypeInfo.isWildcard(): Boolean
    {
        return this.concreteType == Any::class || this == wildcardType
    }


    private fun isCollectionLike(type: TypeInfo): Boolean
    {
        return Collection::class.java.isAssignableFrom(type.concreteType.java)
    }


    private fun isMapLike(type: TypeInfo): Boolean
    {
        return Map::class.java.isAssignableFrom(type.concreteType.java)
    }


    private fun typeArgsMatch(actual: List<TypeInfo>, expected: List<TypeInfo>): Boolean
    {
        if (expected.isEmpty()) return true
        if (actual.isEmpty() && expected.all { it.isWildcard() }) return true
        if (actual.size != expected.size) return false

        return actual.zip(expected).all { (v, e) ->
            e.isWildcard() || (v.concreteType == e.concreteType && typeArgsMatch(v.typeArguments, e.typeArguments))
        }
    }
}