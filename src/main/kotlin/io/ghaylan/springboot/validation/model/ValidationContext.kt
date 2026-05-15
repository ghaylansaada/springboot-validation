package io.ghaylan.springboot.validation.model

import io.ghaylan.springboot.validation.model.errors.ApiError.ErrorLocation
import io.ghaylan.springboot.validation.schema.RequestInputSchema.PropertySpec
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import kotlin.reflect.KClass

/**
 * Contextual state passed through the validation chain for a single field.
 *
 * Carries the field path, active groups, error accumulation policy, and references
 * to the parent object and array for cross-field/cross-element validation.
 *
 * @property fieldPath Dot/bracket path from root (e.g., `"user.address[0].city"`).
 * @property fieldName Simple name of the current field.
 * @property location HTTP section (BODY, QUERY, HEADER, PATH) for error tagging.
 * @property stopOnFirstError When true, stops at the first violation for this field.
 * @property groups Active validation groups that determine which constraints apply.
 */
data class ValidationContext(
	val fieldPath: String,
	val fieldName: String,
	val type: TypeInfo?,
	val location: ErrorLocation,
	val stopOnFirstError: Boolean,
	val groups: Set<KClass<*>>,
	
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
	val array: ValidationContextValue<List<Any>>?,
	
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
	val containerObject: ValidationContextValue<Any>?
)

/**
 * Wraps a value with its schema and type info for cross-field/cross-element rule lookups.
 *
 * @property value Runtime value (e.g., a DTO instance or normalized list of array elements).
 * @property schema Property specs describing the value's fields or elements.
 * @property type Resolved type metadata for the value.
 */
data class ValidationContextValue<T>(
	val value: T?,
	val type: TypeInfo,
	val schema: Map<String, PropertySpec>
)
