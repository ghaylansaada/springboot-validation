package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.map.MapSizeConstraint
import io.ghaylan.springboot.validation.constraints.validators.map.MapSizeValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated map has a number of entries within `[min, max]` (inclusive).
 * Can also be applied to collections of maps; each map is validated independently.
 *
 * @property min Minimum entry count (inclusive, default 0).
 * @property max Maximum entry count (inclusive, default [Int.MAX_VALUE]).
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = MapSizeConstraint::class, validatedBy = [MapSizeValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class MapSize(
	val min: Int = 0,
	val max: Int = Int.MAX_VALUE,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)