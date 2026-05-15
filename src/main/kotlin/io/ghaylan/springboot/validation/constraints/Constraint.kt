package io.ghaylan.springboot.validation.constraints

import kotlin.reflect.KClass

/**
 * Marks an annotation as a validation constraint, linking it to its [ConstraintMetadata] and one or more
 * [ConstraintValidator] implementations. The framework discovers this meta-annotation at startup to build
 * the validator registry.
 *
 * Set [appliesToContainer] to `true` for constraints that operate on the collection/array itself
 * (e.g., `@ArraySize`, `@Distinct`) rather than on individual elements.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Constraint(
	val metadata: KClass<out ConstraintMetadata>,
	val validatedBy: Array<KClass<out ConstraintValidator<out Any, out ConstraintMetadata>>>,
	val appliesToContainer: Boolean = true)