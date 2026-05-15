package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.max.TemporalMaxConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.max.TemporalMaxValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated temporal value does not exceed [value].
 * The [value] string must be in the ISO-8601 format that matches the field's concrete temporal type.
 *
 * @property value ISO-8601 upper-bound string (e.g. `"2025-12-31"` for `LocalDate`).
 * @property inclusive When `true`, allows equality (`<=`); when `false`, enforces strict `<`.
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = TemporalMaxConstraint::class, validatedBy = [TemporalMaxValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class TemporalMax(
	val value: String,
	val inclusive: Boolean,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)