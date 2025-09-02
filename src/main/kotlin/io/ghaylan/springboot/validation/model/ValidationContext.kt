package io.ghaylan.springboot.validation.model

import io.ghaylan.springboot.validation.model.errors.ApiError.ErrorLocation
import io.ghaylan.springboot.validation.schema.RequestInputSchema.PropertySpec
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import java.util.Locale
import kotlin.reflect.KClass

/**
 * Encapsulates metadata and contextual information needed during validation of a request structure.
 *
 * This context is passed through the validation chain to support advanced validation features such as:
 * - Localized error messages.
 * - Conditional validation based on active validation groups.
 * - Cross-field and cross-structure rules (e.g., sibling property dependencies, list uniqueness).
 * - Short-circuiting on first error (fail-fast behavior).
 *
 * It supports recursive schema traversal and is designed to validate both individual fields and structured collections.
 *
 * @property fieldPath Full dot-separated path from the root object to the current property (e.g., "user.address.city").
 * @property fieldName Simple name of the field being validated (e.g., "city").
 * @property location HTTP section where the field appears (e.g., BODY, QUERY, PATH); used for precise error reporting.
 * @property locale Locale used to resolve internationalized error messages.
 * @property stopOnFirstError When true, validation stops on the first constraint violation for this field.
 * @property groups Active validation groups (e.g., Create, Update); determines which constraints apply.
 */
data class ValidationContext(
    val fieldPath: String,
    val fieldName : String,
    val type : TypeInfo?,
    val location: ErrorLocation,
    val locale: Locale,
    val language : String,
    val stopOnFirstError: Boolean,
    val groups : Set<KClass<*>>,

    /**
     * Metadata for the array related to this field, if applicable.
     *
     * This represents:
     * - The **parent array** if the current field is an item within an array.
     * - The **field itself** if the current field is an array (e.g., a list of users).
     *
     * This enables advanced array-related validation features, including:
     * - **Cross-item validation** when validating an element within an array (e.g., sibling uniqueness).
     * - **Recursive and structural validation** when validating an array field (e.g., validating each item).
     *
     * Null if the field is not part of an array and is not an array itself.
     */
    val array : ValidationContextValue<List<Any>>?,

    /**
     * Metadata for the object that directly contains this field (if any).
     * This is populated when the field belongs to a structured object, allowing access to:
     * - The full containing object instance.
     * - The schema of all its properties.
     * - Type metadata for reflection or advanced introspection.
     *
     * This supports **cross-field validation** inside objects, such as:
     * - Conditional requirements based on sibling fields.
     * - Mutual exclusivity or dependency checks.
     *
     * Null when the field is not part of an object (e.g., array of primitives).
     */
    val containerObject : ValidationContextValue<Any>?,
)

/**
 * Wraps a validated value with its schema and type info, enabling rule lookups and structure inspection.
 *
 * Used by [ValidationContext] to carry metadata for parent arrays, current objects, and nested arrays.
 *
 * @property value Actual runtime value being validated (e.g., list of items, DTO, primitive).
 * @property schema Schema definition for the object or collection, usually derived from input schema configuration.
 * @property type Resolved type metadata, including raw class and generic info (used for reflective analysis).
 */
data class ValidationContextValue<T>(
    val value : T?,
    val type : TypeInfo,
    val schema : Map<String, PropertySpec>)
