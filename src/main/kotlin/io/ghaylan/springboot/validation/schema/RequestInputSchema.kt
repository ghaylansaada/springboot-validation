package io.ghaylan.springboot.validation.schema

import io.ghaylan.springboot.validation.accessor.FieldAccessor
import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import kotlin.reflect.KClass

/**
 * The compiled validation schema for a single HTTP controller endpoint, covering body, query params,
 * headers, and path variables. Generated once at startup from reflection and reused at runtime.
 *
 * @property id Unique identifier for this schema (fully-qualified method signature).
 * @property requestBodyTypeInfo Type metadata for the root request body; `null` if no body parameter.
 * @property validationConfig Controls which request parts are validated and how errors are reported.
 */
data class RequestInputSchema(
	val id: String,
	val pathVariables: Map<String, PropertySpec> = emptyMap(),
	val headers: Map<String, PropertySpec> = emptyMap(),
	val queryParams: Map<String, PropertySpec> = emptyMap(),
	val requestBody: Map<String, PropertySpec> = emptyMap(),
	val requestBodyTypeInfo: TypeInfo?,
	val validationConfig: ValidationConfig
) {
	
	/**
	 * Controls which request sections are validated and the error-reporting policy.
	 *
	 * @property singleErrorPerField When `true`, stops at the first failing constraint per field.
	 * @property groups Active validation groups; constraints whose groups don't intersect are skipped.
	 */
	data class ValidationConfig(
		val validateBody: Boolean,
		val validateQuery: Boolean,
		val validateHeaders: Boolean,
		val validatePathVariables: Boolean,
		val singleErrorPerField: Boolean,
		val groups: Set<KClass<*>>
	)
	
	/**
	 * Validation metadata for a single request field: its type, accessor, nested schema, and constraints.
	 *
	 * @property realName The Kotlin source name of the property.
	 * @property resolvedName The effective HTTP name (from `@JsonProperty`, `@RequestParam`, etc.).
	 * @property nested Non-empty for fields that are object types; contains their own [PropertySpec] tree.
	 */
	data class PropertySpec(
		val realName: String,
		val resolvedName: String,
		val typeInfo: TypeInfo,
		val accessor: FieldAccessor<*>,
		val nested: Map<String, PropertySpec>,
		val constraints: Map<ConstraintMetadata, ConstraintValidator<*, *>>
	)
}
