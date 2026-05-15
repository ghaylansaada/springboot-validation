package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.array.size.ArraySizeConstraint
import io.ghaylan.springboot.validation.constraints.validators.array.size.ArraySizeValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated array or collection has a number of elements within `[min, max]` (inclusive).
 *
 * @property min Minimum element count (inclusive, default 0).
 * @property max Maximum element count (inclusive, default [Int.MAX_VALUE]).
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = ArraySizeConstraint::class, validatedBy = [ArraySizeValidator::class], appliesToContainer = true)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class ArraySize(
	val min: Int = 0,
	val max: Int = Int.MAX_VALUE,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)