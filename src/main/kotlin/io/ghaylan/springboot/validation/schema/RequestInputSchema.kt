package io.ghaylan.springboot.validation.schema

import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import io.ghaylan.springboot.validation.accessor.FieldAccessor
import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import kotlin.reflect.KClass

/**
 * Represents the **complete input validation schema** for a single HTTP controller endpoint.
 *
 * This schema defines the expected structure, types, and constraints of all inputs
 * that a controller method accepts, enabling comprehensive validation before
 * the method is invoked.
 *
 * It covers all common request input sources:
 * - **Path variables** (e.g., `/users/{id}`)
 * - **Query parameters** (e.g., `?page=2`)
 * - **Request headers** (e.g., `Authorization`)
 * - **Request body** (complex JSON or form data structures)
 *
 * ## Purpose:
 * Generated at application startup by reflecting over controller method signatures
 * and validation annotations, this schema serves as a blueprint for:
 * - Validating incoming request data at runtime.
 * - Applying group-based validation filtering.
 * - Supporting conditional and selective validation via configuration flags.
 *
 * ## Usage:
 * Validation engines consume this schema to:
 * - Match incoming request parts to expected fields.
 * - Recursively validate nested JSON fields using the defined constraints.
 * - Efficiently filter constraints by validation groups.
 *
 * @property id Unique identifier for the request schema (method class + method name).
 * @property pathVariables Map of path variable names to their respective [PropertySpec] definitions.
 * @property headers Map of expected HTTP header names (case-insensitive) to [PropertySpec].
 * @property queryParams Map of expected query parameter names to [PropertySpec].
 * @property requestBody Map representing the root-level properties of the request body, keyed by JSON property names.
 * @property requestBodyTypeInfo Optional type metadata for the root request body object, used for object-level validations.
 * @property validationConfig Configuration flags controlling which parts to validate and validation behavior.
 */
data class RequestInputSchema(
    val id: String,
    val pathVariables: Map<String, PropertySpec> = emptyMap(),
    val headers: Map<String, PropertySpec> = emptyMap(),
    val queryParams: Map<String, PropertySpec> = emptyMap(),
    val requestBody: Map<String, PropertySpec> = emptyMap(),
    val requestBodyTypeInfo : TypeInfo?,
    val validationConfig : ValidationConfig)
{

    /**
     * Configuration for controlling validation behavior for the associated request schema.
     *
     * Enables toggling validation of individual request parts and configuring
     * constraint evaluation policies such as validation groups and error reporting modes.
     *
     * @property validateBody If true, validates the request body fields.
     * @property validateQuery If true, validates query parameters.
     * @property validateHeaders If true, validates HTTP headers.
     * @property validatePathVariables If true, validates path variables.
     * @property singleErrorPerField If true, stops further validation of a field after the first constraint failure.
     * @property groups A set of validation groups used to filter which constraints are active.
     *   - Constraints without groups are always validated.
     *   - Constraints annotated with specific groups are validated only if their groups intersect with this set.
     */
    data class ValidationConfig(
        val validateBody: Boolean,
        val validateQuery: Boolean,
        val validateHeaders: Boolean,
        val validatePathVariables: Boolean,
        val singleErrorPerField : Boolean,
        val groups: Set<KClass<*>>)


    /**
     * Describes the validation metadata for a single input field within the request.
     *
     * This abstraction supports reflective access, recursive validation of nested fields,
     * and association of constraints with their validator instances.
     *
     * ## Key capabilities:
     * - Reflective reading of the field value during validation using [accessor].
     * - Support for nested properties, allowing deep validation of JSON structures.
     * - Storage of all applicable constraints along with their resolved validator instances.
     * - Dual naming:
     *   - [realName] → the property’s original Kotlin name in source code.
     *   - [resolvedName] → the effective name used in the HTTP request (from annotations).
     * - Type introspection via [typeInfo] enabling precise type-based validation decisions.
     *
     * @property realName The property’s actual name in Kotlin source code.
     * @property resolvedName The name exposed in the HTTP request (header key, query param, path variable, or JSON property).
     * @property typeInfo Detailed type information for this field, including generics and nullability.
     * @property accessor Reflective accessor to retrieve the field’s runtime value.
     * @property nested Nested child properties if this field represents a complex object; empty otherwise.
     * @property constraints Map associating constraint metadata keys to their instantiated validator objects.
     *   - Validators are filtered dynamically based on active validation groups.
     */
    data class PropertySpec(
        val realName: String,
        val resolvedName: String,
        val typeInfo: TypeInfo,
        val accessor: FieldAccessor<*>,
        val nested: Map<String, PropertySpec>,
        val constraints: Map<ConstraintMetadata, ConstraintValidator<*, *>>)
}
