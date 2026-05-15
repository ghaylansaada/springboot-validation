package io.ghaylan.springboot.validation.constraints

import kotlin.reflect.KClass

/**
 * Immutable configuration extracted from a constraint annotation at schema-build time.
 *
 * Subclasses mirror the properties of their corresponding annotation; property names must
 * match exactly so [ConstraintConverter] can map annotation values via reflection.
 *
 * @property groups Active groups; empty means always validate.
 * @property message Optional override for the validator's default error message.
 * @property appliesToContainer Set to `true` for constraints that target the collection/array
 *           itself (e.g., `@ArraySize`, `@Distinct`) rather than individual elements.
 */
abstract class ConstraintMetadata {
	
	abstract val groups: Set<KClass<*>>
	abstract val message: String
	open val appliesToContainer: Boolean = false
}