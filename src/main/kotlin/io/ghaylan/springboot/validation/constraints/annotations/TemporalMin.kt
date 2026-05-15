package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.min.TemporalMinConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.min.TemporalMinValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated temporal value is not before [value].
 * The [value] string must be in the ISO-8601 format that matches the field's concrete temporal type.
 *
 * @property value ISO-8601 lower-bound string (e.g. `"2023-01-01"` for `LocalDate`).
 * @property inclusive When `true` (default), allows equality (`>=`); when `false`, enforces strict `>`.
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = TemporalMinConstraint::class, validatedBy = [TemporalMinValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class TemporalMin(
	val value: String,
	val inclusive: Boolean = true,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)