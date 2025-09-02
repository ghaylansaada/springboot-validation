package io.ghaylan.springboot.validation.engine

import io.ghaylan.springboot.validation.exceptions.ConstraintViolationException
import io.ghaylan.springboot.validation.accessor.FieldAccessor
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeKind
import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import io.ghaylan.springboot.validation.integration.ValidationRegistry
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiError.ErrorLocation
import io.ghaylan.springboot.validation.model.ValidationContextValue
import io.ghaylan.springboot.validation.schema.RequestInputSchema.PropertySpec
import io.ghaylan.springboot.validation.utils.CollectionUtils
import java.util.Locale
import kotlin.collections.ifEmpty
import kotlin.collections.iterator
import kotlin.reflect.KClass

/**
 * A robust, thread-safe validation engine that orchestrates runtime validation of complex, nested data
 * structures using precompiled or dynamically generated schemas. It supports flexible validation strategies,
 * precise error reporting, and context-sensitive validation for DTOs and HTTP requests.
 *
 * ## Overview
 *
 * The `ValidatorEngine` is the core runtime component of a validation framework designed to validate:
 * - **DTOs**: Arbitrary Kotlin objects with nested fields and collections.
 * - **HTTP Requests**: Body, query parameters, headers, and path variables.
 * - **Cross-Field/Element Rules**: Constraints that depend on multiple fields or collection elements.
 *
 * It integrates with Spring Boot, supports custom constraints, and provides detailed error reporting
 * with precise field paths (e.g., `user.address[0].city`) and localized messages.
 *
 * ## Key Features
 *
 * ### 1. Multi-Context Validation
 * - Validates DTOs, HTTP request components, and cross-field/element relationships.
 * - Supports arrays of any depth (e.g., `List<List<User>>`).
 * - Handles `null` values and empty collections predictably.
 *
 * ### 2. Precise Error Reporting
 * - Tracks validation failures using canonical paths:
 *   - Nested objects: `user.profile.email`
 *   - Arrays: `users[0].addresses[1].city`
 *   - Root arrays: `[0].name`
 * - Tags errors with their source ([ErrorLocation.BODY], [ErrorLocation.QUERY], etc.).
 * - Supports localized error messages via [Locale].
 *
 * ### 3. Flexible Validation Modes
 * - **Fail-Fast**: Stops at the first error per field for compact responses.
 * - **Comprehensive**: Collects all errors for detailed feedback (e.g., form validation).
 *
 * ### 4. Validation Groups
 * - Supports context-sensitive validation using groups (e.g., `CreateGroup`, `UpdateGroup`).
 * - Defaults to [DefaultGroup] if no groups are specified.
 *
 * ### 5. Performance Optimizations
 * - **Stateless Design**: Thread-safe with no mutable state.
 * - **Efficient Access**: Uses [FieldAccessor] for fast, precomputed property access.
 * - **Zero-Copy**: Minimizes allocations for collection normalization.
 * - **Early Termination**: Stops validation when appropriate in fail-fast mode.
 *
 * ## Schema Resolution
 *
 * Schemas are managed via [ValidationRegistry]:
 * - **Class-based (DTO Validation)**: For the `validate` function, schemas are resolved by class type.
 *   If no schema exists for a type, the engine dynamically creates and caches one for subsequent use.
 * - **Route-based (HTTP Request Validation)**: For the `validateRequest` function, schemas must be
 *   pre-registered for the given controller method. Missing schemas result in an
 *   `IllegalStateException`.
 *
 * Dynamic schema creation ensures flexibility for DTO validation, while pre-cached schemas provide
 * predictable performance for HTTP request validation.
 *
 * ## Usage
 *
 * ### 1. DTO Validation
 * Validates a single object or collection against its schema, dynamically creating one if needed.
 *
 * ```kotlin
 * data class User(
 *     @field:Required @field:Email val email: String?,
 *     @field:Size(min = 8) val password: String?)
 *
 * val engine = ValidatorEngine(validationContainer)
 * engine.validate<User>(
 *     params = user,
 *     groups = arrayOf(CreateGroup::class),
 *     singleErrorPerField = false)
 * ```
 *
 * ### 2. HTTP Request Validation
 * Validates all parts of an HTTP request against a pre-registered schema.
 *
 * ```kotlin
 * engine.validateRequest(
 *     id = "xxx",
 *     body = userDto,
 *     params = mapOf("page" to "1"),
 *     headers = mapOf("Authorization" to "Bearer token"),
 *     pathVariables = mapOf("id" to "123"),
 *     locale = Locale.FRENCH)
 * ```
 *
 * ## Error Handling
 *
 * - Throws [ConstraintViolationException] with deduplicated [ApiError]s on validation failure.
 * - Errors include field path, error code, location, and localized message.
 * - Deduplication ensures one error per unique field, code, and location combination.
 *
 * ## Thread Safety
 *
 * Fully thread-safe:
 * - No mutable state beyond method-local variables.
 * - Safe for concurrent use across multiple threads/requests.
 *
 * ## Integration
 *
 * - **Custom Constraints**: Extensible via [ConstraintValidator] implementations.
 * - **AOP**: Can be used with aspect-oriented programming for automatic validation.
 *
 * ## Limitations
 *
 * - HTTP request validation requires pre-registered schemas in [ValidationRegistry].
 * - Only validates objects or arrays of objects/scalars; scalars as root DTOs are not supported.
 *
 * @param validationRegistry The schema repository for resolving and caching validation metadata.
 */
open class ValidatorEngine(val validationRegistry : ValidationRegistry)
{
    /**
     * Represents a unique key for identifying a [ApiError] using a combination of its [field], [code], and [location].
     *
     * This is primarily used for deduplication of validation errors, ensuring that
     * only one error per unique combination is retained.
     *
     * - [field]: The name of the field that caused the violation. Can be null for root-level errors.
     * - [code]: The validation constraint code (e.g. `"Required"`, `"Email"`, etc.). May vary in type.
     * - [location]: The location in the request body or path where the error occurred (e.g. nested object path).
     */
    data class ErrorKey(val field: String?, val code: Any?, val location: ErrorLocation?)



    /**
     * Validates a Kotlin object or collection of type [T] against its registered or dynamically created schema.
     *
     * This is the primary entry point for validating a single object or collection outside of HTTP request
     * orchestration. The engine resolves the schema for [T] from [validationRegistry], dynamically creating
     * and caching one if none exists, then constructs a root [ValidationContext] for recursive validation.
     *
     * ### Features
     * - Dynamically generates and caches a schema for [T] if not already registered in [validationRegistry].
     * - Supports nested objects and collections (including multi-dimensional arrays).
     * - Collects all [ApiError]s and throws a [ConstraintViolationException] if validation fails.
     * - Supports validation groups for context-sensitive rules (e.g., `CreateGroup`, `UpdateGroup`).
     * - Offers fail-fast (one error per field) or comprehensive (all errors) modes.
     *
     * ### Validation Groups
     * Controls which constraints are evaluated based on their group annotations.
     * Only constraints belonging to groups intersecting with [groups] are checked.
     * By default, this includes only the [DefaultGroup].
     *
     * Typical usages:
     * - `groups = arrayOf(DefaultGroup::class)` — default validation rules.
     * - `groups = arrayOf(CreateGroup::class)`  — stricter rules for creation flows.
     * - `groups = arrayOf(UpdateGroup::class)`  — relaxed rules for partial updates.
     *
     * ### Error Accumulation Policy
     * - When [singleErrorPerField] is `true`, validation stops at the first failing constraint *per field*,
     *   producing at most one error message per logical field path.
     * - When `false`, all constraints are evaluated and every violation is accumulated,
     *   which is useful for comprehensive UI feedback or batch processing.
     *
     * ### Parameters
     * @param params The object or collection to validate (may be `null`).
     * @param location The source of the data (default: [ErrorLocation.BODY]).
     * @param locale The locale for error messages (default: [Locale.ENGLISH]).
     * @param singleErrorPerField If `true`, stops at the first error per field; if `false`, collects all errors.
     * @param groups Validation groups to apply (default: [DefaultGroup]).
     *
     * ### Throws
     * @throws IllegalStateException If schema creation fails for [T].
     * @throws ConstraintViolationException If validation fails.
     *
     * ### Notes
     * - Supports nested objects and collections via recursive calls to [validate].
     * - For HTTP request validation, prefer using [validateRequest], which requires pre-registered schemas.
     */
    suspend inline fun <reified T> validate(
        params : T?,
        location : ErrorLocation = ErrorLocation.BODY,
        locale : Locale = Locale.ENGLISH,
        singleErrorPerField : Boolean = true,
        groups : Array<KClass<*>> = arrayOf(DefaultGroup::class))
    {
        // Resolve the compiled schema for T (type info + field specs). Must be registered beforehand.
        val schema = validationRegistry.resolveSchemaByClass(T::class.java)

        // Accumulate field-level errors found during traversal.
        val errors = mutableListOf<ApiError>()

        // Construct the root validation context; this is the baseline propagated through recursion.
        val context = ValidationContext(
            // Root context has no specific field name.
            fieldName = "",
            // Root path starts empty; built upon during recursion.
            fieldPath = "",
            type = null,
            // Tag errors with the logical source location.
            location = location,
            // Locale for message resolution.
            locale = locale,
            language = standardizeLanguage(locale),
            // Error accumulation policy.
            stopOnFirstError = singleErrorPerField,
            // Active validation groups.
            groups = groups.toSet(),
            array = null,
            // Provide the current object context so cross-field rules at root can inspect siblings if needed.
            containerObject = ValidationContextValue(
                // The root object(s) under validation.
                value = params,
                // Field schema map for T.
                schema = schema.second,
                // TypeInfo describing T.
                type = schema.first))

        // Delegate validation to the internal recursive function.
        validate(
            value = params,
            type = schema.first,
            fields = schema.second,
            context = context,
            errors = errors)

        deduplicateErrors(errors)
    }


    /**
     * Returns a standardized language tag in the format `"ll-CC"`,
     * where `ll` is the ISO 639-1 two-letter lowercase language code
     * and `CC` is the ISO 3166-1 two-letter uppercase country code.
     *
     * This method is useful for producing consistent locale identifiers
     * that can be used in internationalization (i18n), resource lookup,
     * and localization-aware logic.
     *
     * Example:
     * ```kotlin
     * standardizeLanguage(Locale("en", "US")) // "en-US"
     * standardizeLanguage(Locale("fr", "CA")) // "fr-CA"
     * ```
     *
     * @param locale The locale from which to extract the language and country codes.
     * @return A string in `"ll-CC"` format representing the given locale.
     */
    fun standardizeLanguage(locale: Locale) : String
    {
        return "${locale.language}-${locale.country}"
    }


    /**
     * Validates all enabled parts of an incoming HTTP request against its registered validation schema.
     *
     * ---
     * ### Overview
     * This function is the **main entry point** for runtime request validation. It:
     * 1. Resolves the precompiled validation schema for the given controller method.
     * 2. Constructs a base [ValidationContext] used across all request sections (body, query, headers, path).
     * 3. Delegates to specialized validation functions depending on the section type:
     *    - **Body**: Recursive validation via [validate], supports objects, arrays, and deep nesting.
     *    - **Query / Headers / Path**: Flat-map validation via [validateHeadersOrParamsOrPathVariables].
     *
     * ---
     * ### Supported Shapes
     * - **Body**: Object or array of objects (including multi-dimensional arrays of scalars/objects).
     * - **Query / Headers / Path**: Flat key-value maps; scalar arrays supported (e.g., `tags=red&tags=blue`).
     *
     * ---
     * ### Example Error Paths
     * - Top-level array of objects (body): `"[0].email"`
     * - Nested array of scalars: `"colors[1][2]"`
     * - Query/header/path parameter: `"token"` or `"tags[0]"`
     *
     * ---
     * ### Parameters
     * @param id             The unique identifier for the request being validated.
     * @param body           The deserialized request body (can be object, list, nested array, or null).
     * @param params         Query parameters as a flat map.
     * @param headers        HTTP headers as a flat map.
     * @param pathVariables  Path variables as a flat map (e.g., `id` from `"/users/{id}"`).
     * @param locale         Locale used for resolving error messages. Defaults to `Locale.ENGLISH`.
     *
     * ---
     * ### Throws
     * @throws IllegalStateException If no schema is registered for the route, or if body validation is enabled but missing a type definition.
     *
     * ---
     * ### Notes
     * - Each section is validated **independently**.
     * - The **body** section uses full recursive validation; scalar and object arrays of any depth are supported.
     * - All errors are collected and returned as a single exception to simplify client handling.
     */
    suspend fun validateRequest(
        id : String,
        body: Any?,
        params : Map<String, Any?>?,
        headers : Map<String, Any?>?,
        pathVariables : Map<String, Any?>?,
        locale : Locale = Locale.ENGLISH
    ) : List<ApiError>
    {
        // 1) Resolve schema for this route+verb combination. Fail fast if not registered.
        val schema = validationRegistry.getSchemaByRequest(id) ?: error("No validation schema found for request $id.")

        // 2) Create a list to collect validation errors across all sections.
        val errors = mutableListOf<ApiError>()

        // 3) Build a "base" context to be copied and specialized per request section.
        val baseCtx = ValidationContext(
            // root has no logical name
            fieldName = "",
            // path will be set during recursion
            fieldPath = "",
            type = null,
            // overridden per section
            location = ErrorLocation.BODY,
            locale = locale,
            language = standardizeLanguage(locale),
            // validation groups to enforce
            groups = schema.validationConfig.groups.toSet(),
            // error accumulation policy
            stopOnFirstError = schema.validationConfig.singleErrorPerField,
            // root has no parent array
            array = null,
            // will be set per section
            containerObject = null)

        // ---------------- Body ----------------
        if (schema.validationConfig.validateBody)
        {
            // Uses validate() which handles both objects and arrays (incl. multi-dimensional).
            // `forceNonEmpty = true` ensures empty or null root arrays still validate index [0].

            validate(
                value = body,
                type = schema.requestBodyTypeInfo!!,
                fields = schema.requestBody,
                context = baseCtx.copy(location = ErrorLocation.BODY, type = schema.requestBodyTypeInfo),
                errors = errors)
        }

        // ---------------- Query ----------------
        if (schema.validationConfig.validateQuery)
        {
            // Build a currentObject wrapper for the query param map
            val currentValueCtx = ValidationContextValue<Any>(
                value = params,
                schema = schema.queryParams,
                type = TypeInfo(
                    // high-level type: Map
                    rawRootType = Map::class,
                    // concrete type also Map
                    concreteType = Map::class,
                    // categorize as map for schema logic
                    kind = TypeKind.MAP))

            // Specialize context for QUERY section
            val queryCtx = baseCtx.copy(location = ErrorLocation.QUERY, containerObject = currentValueCtx)

            // Validate query params using flat-map validator
            validateHeadersOrParamsOrPathVariables(
                params = params,
                schema = schema.queryParams,
                context = queryCtx,
                errors = errors)
        }

        // ---------------- Headers ----------------
        if (schema.validationConfig.validateHeaders)
        {
            // Build currentObject wrapper for headers map
            val currentValueCtx = ValidationContextValue<Any>(
                value = headers,
                schema = schema.headers,
                type = TypeInfo(
                    // high-level type: Map
                    rawRootType = Map::class,
                    // concrete type also Map
                    concreteType = Map::class,
                    // categorize as map for schema logic
                    kind = TypeKind.MAP))

            // Specialize context for HEADER section
            val headerCtx = baseCtx.copy(location = ErrorLocation.HEADER, containerObject = currentValueCtx)

            // Validate HTTP headers using flat-map validator
            validateHeadersOrParamsOrPathVariables(
                params = headers,
                schema = schema.headers,
                context = headerCtx,
                errors = errors)
        }

        // ------------- Path Variables ------------
        if (schema.validationConfig.validatePathVariables)
        {
            // Build currentObject wrapper for path variables map
            val currentValueCtx = ValidationContextValue<Any>(
                value = pathVariables,
                schema = schema.pathVariables,
                type = TypeInfo(
                    // high-level type: Map
                    rawRootType = Map::class,
                    // concrete type also Map
                    concreteType = Map::class,
                    // categorize as map for schema logic
                    kind = TypeKind.MAP))

            // Specialize context for PATH section
            val pathVariableCtx = baseCtx.copy(location = ErrorLocation.PATH, containerObject = currentValueCtx)

            // Validate path variables using flat-map validator
            validateHeadersOrParamsOrPathVariables(
                params = pathVariables,
                schema = schema.pathVariables,
                context = pathVariableCtx,
                errors = errors)
        }

        return deduplicateErrors(errors)
    }


    /**
     * Deduplicates and return constraint validation errors.
     *
     * This function ensures unique [ApiError] entries by eliminating duplicates
     * based on a combination of:
     * - [ApiError.path]
     * - [ApiError.code]
     * - [ApiError.location]
     *
     * This is optimized for large lists and preserves the first occurrence of each unique error.
     * Use this at the end of validation (e.g., in `validate()` or `validateRequest()`).
     */
    fun deduplicateErrors(errors: List<ApiError>) : List<ApiError>
    {
        if (errors.isEmpty()) return errors

        val seen = HashSet<ErrorKey>()
        val unique = ArrayList<ApiError>(errors.size)

        for (error in errors)
        {
            val key = ErrorKey(error.path, error.code, error.location)

            if (seen.add(key)) {
                unique.add(error)
            }
        }

        return unique
    }


    /**
     * Entrypoint to validate:
     * 1. A **single DTO object** and all its nested properties.
     * 2. An **array/list of DTOs** (including multi-dimensional arrays) and their contents.
     *
     * ---
     * ### What this does
     * This function delegates validation based on the runtime structure of [value]:
     * - If [value] is an array/list (of any depth), it is handled by [validateArray].
     * - If [value] is a single object, its properties are validated via [validateFields].
     * - Scalars or non-object/array inputs are considered invalid and will raise an error.
     *
     * ---
     * ### Parameters
     * @param value     The actual runtime input (could be a DTO object, list/array, or `null`).
     * @param type      [TypeInfo] describing the expected shape of [value]: object or array.
     * @param fields    A map describing the schema of properties if [value] is an object (from compiled constraints).
     * @param context   [ValidationContext] tracking the validation path, source location, and active constraint groups.
     * @param errors    Mutable accumulator collecting any [ApiError]s during recursive traversal.
     *
     * ---
     * ### Delegation Behavior
     *
     * - **If [type] is an array/list**:
     *   - Calls [validateArray] with `forceNonEmpty = true`.
     *   - This ensures that even empty arrays or `null` values generate child paths like `"[0]"` for accurate error reporting.
     *   - This is crucial **only** when the root DTO is an array, to distinguish between a missing value and an empty one.
     *   - If the root is an object, missing or empty arrays in its fields are handled by the field-level logic in [validateFields].
     *
     * - **If [type] is an object**:
     *   - Calls [validateFields] to validate each property recursively.
     *   - Handles deeply nested fields and enforces constraints like `@Required`, `@Email`, etc., on each one.
     *
     * - **If neither**:
     *   - Throws an `IllegalStateException` — this validation engine expects DTOs (objects or arrays of objects).
     *
     * ---
     * ### Example Usages
     *
     * ```kotlin
     * // 1. Array of users
     * validate(users, userListTypeInfo, userSchema, context, errors)
     * // → produces paths like: "[0].email", "[1].password"
     *
     * // 2. Nested arrays of users
     * validate(matrix, matrixTypeInfo, userSchema, context, errors)
     * // → produces: "[0][1].email"
     *
     * // 3. Single user object
     * validate(user, userTypeInfo, userSchema, context, errors)
     * // → produces: "email", "password"
     * ```
     */
    suspend fun validate(
        value : Any?,
        type : TypeInfo,
        fields: Map<String, PropertySpec>,
        context: ValidationContext,
        errors: MutableList<ApiError>)
    {
        if (type.isArray)
        {
            // 🔁 Root is a list/array → recursively validate all elements (including nested arrays).
            // `forceNonEmpty = true` ensures that even empty lists will trigger element-level validation,
            // producing paths like "[0]" to indicate missing items when root is array.

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
        else if (type.isObject)
        {
            // 🔍 Root is a DTO → validate all its properties recursively.
            // Handles nested DTOs, field-level constraints, and path tracking.
            validateFields(
                param = value,
                type = type,
                parentType = null,
                fields = fields,
                context = context,
                errors = errors)
        }
        else
        {
            // ❌ Scalars or primitive types are invalid as root DTOs.
            // The engine is built to validate objects or arrays of objects only.

            error("Param must be an object or an array of objects to be validated.")
        }
    }


    /**
     * Recursively validates an array or list of any depth and element type.
     *
     * Supports:
     * - **Scalars** (e.g., `List<String>`, `IntArray`)
     * - **Objects** (e.g., `List<User>`)
     * - **Nested arrays** (e.g., `List<List<User>>`, `Array<Array<Int>>`)
     *
     * ---
     * ### What this does
     * 1. Normalizes the input into a read-only list (zero-copy when possible).
     * 2. Applies **container-level** validations (e.g., size, uniqueness) using [constraints].
     * 3. Iterates over elements and:
     *    - If element is an array → recurses via `validateArray()`.
     *    - If element is an object → recurses via `validateFields()`.
     *    - If element is a scalar → validates via `validateValue()`.
     *
     * ---
     * ### Multi-dimensional arrays
     * The method determines array depth dynamically using [TypeInfo.typeArguments].
     * Each recursive call handles **exactly one dimension**, for example:
     * - `List<String>` → validates individual strings.
     * - `List<List<String>>` → validates outer list, then each inner list, then each string.
     * - `List<List<User>>` → validates lists, then fields within each `User` object.
     *
     * ---
     * ### Error path tracking
     * The [ValidationContext.fieldPath] is extended at every depth level using [appendIndex], enabling precise error localization:
     * - Arrays of objects → `"[0].email"`
     * - Nested scalar arrays → `"colors[1][2]"`
     * - Deep nesting → `"matrix[2][1][0]"`, etc.
     *
     * ---
     * ### `forceNonEmpty` flag
     * If `true`, treats a `null` or empty array as if it contains a single `null` element at index `[0]`.
     * This triggers element-level constraints such as `@Required` even on empty collections.
     *
     * - **Important nuance**: This flag is typically used **only when the root DTO is an array**.
     *   - If the root is a scalar or object, an empty array is treated as a missing field or value entirely.
     *   - But if the **root itself is an array**, `forceNonEmpty` ensures at least one "missing element" is validated to catch emptiness as an error.
     *
     * ---
     * ### Parameters
     * @param params        The input array or list (nullable, may be empty).
     * @param type          [TypeInfo] describing the current array level.
     * @param schema        Schema for object elements (ignored for scalars).
     * @param context       Active [ValidationContext] (path, metadata, etc.).
     * @param errors        Shared mutable list to collect [ApiError]s.
     * @param forceNonEmpty If `true`, injects a single `null` element into empty arrays to ensure non-emptiness is validated (see above).
     * @param constraints   Constraints applied to the array and/or its elements.
     *
     * ---
     * ### Behavior notes
     * - Enforces constraints across elements (e.g., size, uniqueness).
     * - Maintains original element order (`[0]` to `[n-1]`).
     * - Stops recursion once scalars or terminal object types are reached.
     *
     * ---
     * ### Performance
     * - **Time:** O(n) per array level (recursively applied).
     * - **Space:** O(d), where *d* is the nesting depth.
     */
    private suspend fun validateArray(
        params: Any?,
        type: TypeInfo,
        schema: Map<String, PropertySpec>,
        context: ValidationContext,
        errors: MutableList<ApiError>,
        forceNonEmpty: Boolean,
        constraints: Map<ConstraintMetadata, ConstraintValidator<*, *>>)
    {
        // Element type at the next level (may be scalar, object, map, or another array).
        val elementType = type.typeArguments.first()

        // Normalize current array (or treat empty arrays as a single null element if forceNonEmpty is true).
        val elements = CollectionUtils.normalizeList(params).let {
            if (forceNonEmpty) it.ifEmpty { listOf(Any()) } else it
        }

        // Context value representing the current array container (for cross-element validation).
        val arrayValueCtx = ValidationContextValue(
            value = elements,
            schema = schema,
            type = type)

        suspend fun validateAsContainerIfApplicable()
        {
            if (constraints.any { it.key.appliesToContainer }) {

                validateValue(
                    value = params,
                    context = context,
                    errors = errors,
                    constraints = constraints)
            }
        }

        when
        {
            // ----- Case 1: Current level is an array-of-arrays (multi-dimensional) -----
            type.isArrayOfArrays -> {

                validateAsContainerIfApplicable()

                elements.forEachIndexed { idx, element ->

                    // Update the field path with the current index (e.g., "[0]" → "[0][1]").
                    val nestedCtx = context.copy(
                        fieldPath = appendIndex(context.fieldPath, idx),
                        // for cross-element validation
                        array = arrayValueCtx,
                        // arrays don't have properties
                        containerObject = null)

                    // Recurse into the nested array.
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
                        // For cross-element validation
                        array = arrayValueCtx)

                    // Recurse into the nested object's fields
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
                        // scalars have no nested properties
                        containerObject = null)

                    // Apply constraints to the scalar element.
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
     * Recursively validates all declared fields on an object or container type.
     *
     * This is the **core traversal routine** that powers nested validation. It walks each [PropertySpec]
     * in the given [fields] map and applies validations in a depth-first manner.
     *
     * ---
     * ### What this does
     * For every declared field:
     * 1. Resolves its runtime value via the accessor (or `null` if the parent is null).
     * 2. Builds a field-scoped [ValidationContext] that updates:
     *    - `fieldPath` (e.g., `"user.address.city"`)
     *    - `currentObject` for cross-field validations
     *    - `nestedArray` if the field is an array/list
     * 3. Validates scalar constraints via [validateValue] (e.g., `@Required`, `@Pattern`, etc.).
     * 4. Recursively delegates validation if the field is:
     *    - an array → to [validateArray]
     *    - a nested object → back to this function
     *
     * ---
     * ### Context propagation
     * Each field’s [ValidationContext] is independently constructed, ensuring:
     * - Full path tracking (`user.friends[2].email`)
     * - Correct group and locale inheritance
     * - Scoped object/array context for cross-field or cross-element rules
     *
     * ---
     * ### Parameters
     * @param param   The object instance whose fields are being validated (nullable).
     * @param type    Runtime type info for the parent object ([TypeInfo]).
     * @param parentType  Runtime type info for the parent object's parent (nullable).
     * @param fields  Validation schema describing all declared fields ([PropertySpec]s).
     * @param context Validation context carrying path, groups, and execution state.
     * @param errors  Shared accumulator for [ApiError] instances encountered during validation.
     */
    private suspend fun validateFields(
        param : Any?,
        type : TypeInfo,
        parentType : TypeInfo?,
        fields : Map<String, PropertySpec>,
        context : ValidationContext,
        errors : MutableList<ApiError>)
    {
        // Iterate through each field defined in the schema
        fields.forEach { (_, field) ->

            // Resolve the runtime value via the field accessor.
            // If the parent object is null, value is necessarily null.
            val value = param?.let { field.accessor.getFromAny(it, strict = false) }

            // If the field is an array/list, build a read-only, indexable view for cross-element rules.
            // NOTE: zero‑copy when possible; nulls are filtered only if present.
            val arrayValueCtx = if (field.typeInfo.isArray)
            {
                ValidationContextValue(
                    // normalized list of elements (non-null)
                    value = CollectionUtils.normalizeList(value),
                    // schema for nested elements (if any)
                    schema = field.nested,
                    // type info for the array
                    type = field.typeInfo)
            }
            // If current type is object or map that have a reference to its parent array (if exists) then delegate it
            // to the items of the array for cross-element validation.
            else if ((type.isObject || type.isMap) && (parentType?.isArrayOfObjects == true || parentType?.isArrayOfMaps == true))
            {
                context.array
            }
            else null

            // Build a current-object context for cross-field validations (sibling awareness).
            val objectValueCtx = ValidationContextValue(
                // parent object that holds this field
                value = param,
                // schema of all siblings (for cross-field rules)
                schema = fields,
                // type info of the parent object
                type = type)

            // Derive the field-scoped ValidationContext (updated name/path + contexts).
            val fieldCtx = context.copy(
                // name of the current field
                fieldName = field.resolvedName,
                // full path (parent.path.fieldName)
                fieldPath = appendPath(context.fieldPath, field.resolvedName),
                type = field.typeInfo,
                // parent context for cross-field rules
                containerObject = objectValueCtx,
                // array context if field is an array, for cross-element rules
                array = arrayValueCtx)

            // Execute field-level constraints (presence, type, semantic).
            // Honors `stopOnFirstError` via validateValue().
            validateValue(
                value = value,
                context = fieldCtx,
                errors = errors,
                constraints = field.constraints)

            // If the value is null, there’s nothing deeper to validate (stop here)
            if (value == null) return@forEach

            // Descend further based on the field’s shape:
            // - Arrays/lists → delegate to validateArray (elements + nested objects if any).
            // - Single nested object → recurse into its fields.
            if (field.typeInfo.isArray)
            {
                // Arrays/lists (of scalars or objects): validate container + elements.
                // `forceNonEmpty = true` ensures constraints on the element itself can fire consistently.
                validateArray(
                    params = value,
                    type = field.typeInfo,
                    schema = field.nested,
                    context = fieldCtx,
                    errors = errors,
                    forceNonEmpty = true,
                    constraints = field.constraints)
            }
            else if (field.typeInfo.isObject)
            {
                // Single nested object: recurse into its declared fields.
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


    /**
     * Validates a flat collection of key-value pairs such as query parameters, HTTP headers, or path variables.
     *
     * This function performs validation on each parameter defined in the provided [schema], applying all
     * configured constraints to the corresponding value(s) found in [params].
     *
     * Key characteristics:
     * - The input [params] is a simple flat map from parameter names to their values.
     * - Values can be single (scalar) or multi-valued (arrays/collections).
     * - Nested or hierarchical structures within parameter values are not traversed or validated here.
     * - For multi-valued scalar parameters (arrays), each element is validated individually.
     *
     * ---
     * ### Validation context
     * - For each parameter, a dedicated [ValidationContext] is created with a `fieldName` and `fieldPath`
     *   corresponding to the parameter name.
     * - When validating array elements, the context is updated with indexed paths (`paramName[index]`)
     *   and includes a reference to the parent array context for potential cross-element validation.
     *
     * @param params The map of parameter names to values to validate; may be null if no parameters are present.
     * @param schema The validation schema describing constraints and metadata for each parameter.
     * @param context The current validation context, used to propagate location, locale, and grouping information.
     * @param errors A mutable list that collects all validation errors discovered during execution.
     */
    @Suppress("KDocUnresolvedReference")
    private suspend fun validateHeadersOrParamsOrPathVariables(
        params : Map<String, Any?>?,
        schema : Map<String, PropertySpec>,
        context: ValidationContext,
        errors: MutableList<ApiError>)
    {
        // If no schema rules exist, skip validation entirely.
        if (schema.isEmpty()) return

        // Iterate over each parameter described by the schema.
        schema.forEach { (_, param) ->

            // Retrieve the runtime value for the parameter from the input map.
            val value = params?.get(param.resolvedName)

            // Create a new ValidationContext scoped to this parameter.
            // Note: fieldPath here is flat (no dots) since parameters are top-level.
            val paramCtx = context.copy(fieldName = param.resolvedName, fieldPath = param.resolvedName)

            // Validate the parameter's overall value (could be scalar or entire collection).
            validateValue(
                value = value,
                context = paramCtx,
                errors = errors,
                constraints = param.constraints)

            // If the parameter represents a scalar array (e.g., list of strings),
            // validate each element separately.
            if (param.typeInfo.isArrayOfScalars)
            {
                // Normalize the value to a list of non-null elements for iteration.
                val elements = CollectionUtils.normalizeList(value)

                // Construct a parent array context that holds the entire normalized list,
                // providing a container for cross-element validation if needed.
                val parentArray = ValidationContextValue(
                    // list of non-null scalar elements
                    value = elements,
                    // no nested schema for scalar values
                    schema = emptyMap(),
                    // type metadata of the scalar array parameter
                    type = param.typeInfo)

                // Iterate over each element, validating individually.
                elements.forEachIndexed { idx, elem ->

                    // Create a context for this element with an indexed field path like paramName[0].
                    // Pass parent array context for potential cross-element constraints.
                    // currentObject is null since scalars don't have properties.
                    val elemCtx = paramCtx.copy(
                        fieldPath = appendIndex(paramCtx.fieldPath, idx),
                        array = parentArray,
                        containerObject = null)

                    // Validate the single element against the parameter constraints.
                    validateValue(
                        value = elem,
                        context = elemCtx,
                        errors = errors,
                        constraints = param.constraints)
                }
            }
        }
    }


    /**
     * Appends a field (property) name to an existing dot-delimited path.
     *
     * This helper is used exclusively to build the logical path for **nested fields** in objects.
     * It guarantees a consistent and predictable dot-delimited format across the validation engine.
     *
     * **Format rules:**
     * - If the `child` is empty, the existing `base` path is returned unchanged.
     * - If the `base` is empty (root level), the `child` is returned directly (no leading dot).
     * - Otherwise, the two segments are joined with a dot: `"base.child"`.
     *
     * **Examples**
     * - `appendPath("", "user")`        → `"user"`
     * - `appendPath("user", "address")` → `"user.address"`
     * - `appendPath("user.address", "")`→ `"user.address"` (child is ignored)
     *
     * **Notes**
     * - Inputs are expected to be canonical field names (no trimming or sanitization is done).
     * - Complexity: O(|base| + |child|); a single result string is allocated.
     *
     * @param base   The existing dot-delimited field path (or empty string at the root).
     * @param child  The next field name to append (may be empty to skip).
     * @return       A new field path string that combines both segments.
     */
    private fun appendPath(base: String, child: String): String
    {
        // If child is empty, we skip appending and return the base path unchanged.
        if (child.isEmpty()) return base

        // If base is empty (root level), the resulting path is simply the child field name.
        if (base.isEmpty()) return child

        // Otherwise, join the base and child segments with a dot to form "base.child".
        return "$base.$child"
    }


    /**
     * Appends an array index segment to a field path.
     *
     * The engine represents logical payload locations using a dot-delimited path for object properties
     * and square brackets for array indices. This helper appends a single index to an existing path.
     *
     * **Format rules:**
     * - If the base path is empty (root), the result is simply `"[idx]"`.
     * - Otherwise, the index is appended directly: `"base[idx]"`.
     *
     * **Examples**
     * - `appendIndex("", 0)`      → `"[0]"`
     * - `appendIndex("user", 1)`  → `"user[1]"`
     * - `appendIndex("users[2].emails", 3)` → `"users[2].emails[3]"`
     *
     * **Why this exists**
     * Centralizing path formatting guarantees a stable, predictable path representation
     * across the whole validation engine (for logs, client error payloads, tests, etc.).
     *
     * @param base Existing field path (empty string when at the root).
     * @param idx  Zero-based array index to append.
     * @return The composed path including the new index segment.
     */
    private fun appendIndex(base: String, idx: Int): String
    {
        // If we're at the root (empty path), return only the bracketed index: "[idx]"
        // Otherwise, append "[idx]" to the existing path (no dot before bracketed indices).
        return if (base.isEmpty()) "[$idx]" else "$base[$idx]"
    }


    /**
     * Validates a single field value against its associated set of constraints.
     *
     * This method is responsible for executing each [ConstraintValidator] attached to a field,
     * in the order defined by the [constraints] map. For each constraint, it evaluates the given
     * [value] in the current [context], and appends any resulting validation errors to [errors].
     *
     * ### Behavior
     * - If [constraints] is empty, validation is skipped immediately.
     * - Honors [ValidationContext.stopOnFirstError]:
     *   * `true`: Halts evaluation at the first failing constraint, adding only one error.
     *   * `false`: Evaluates all constraints, accumulating one [ApiError] per failure.
     *
     * ### Parameters
     * @param value      The field's runtime value to validate. May be `null`.
     * @param context    Contextual information about the current field (path, parent object, etc.).
     * @param errors     A shared mutable collection that aggregates all validation errors found so far.
     * @param constraints A mapping of [ConstraintMetadata] (describes the rule) to the corresponding
     *                    [ConstraintValidator] (executes the rule).
     *
     * ### Performance note
     * - The constraints are assumed to be in a predictable order (e.g., Required → Semantic)
     *   as defined at schema compilation. No reordering is done here for speed.
     */
    private suspend fun validateValue(
        value: Any?,
        context: ValidationContext,
        errors: MutableList<ApiError>,
        constraints: Map<ConstraintMetadata, ConstraintValidator<*, *>>)
    {
        // --- Early exit: no constraints to run ---
        if (constraints.isEmpty()) return

        // --- Mode 1: stop on first error (used for compact error payloads) ---
        if (context.stopOnFirstError)
        {
            // Iterate over each constraint in the order it was defined
            for (constraint in constraints)
            {
                // Ask the validator to validate the current value in the current context
                val error = constraint.value.runValidation(value, constraint.key, context)

                // If validation failed (returned an error), record it and stop fu
                if (error != null)
                {
                    // add the error to the accumulator list
                    errors += error
                    // short-circuit: no further constraints are evaluated for this field
                    return
                }
            }
        }
        // --- Mode 2: accumulate all errors (used for rich error reporting) ---
        else
        {
            // Iterate over all constraints and add any error returned by the validator
            constraints.forEach {
                // validate() returns null if success, an ApiError if validation fails
                it.value.runValidation(value, it.key, context)?.let(errors::add)
            }
        }
    }
}