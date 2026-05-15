package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.past.PastConstraint
import io.ghaylan.springboot.validation.constraints.validators.temporal.past.PastValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated temporal value is strictly in the past.
 * The optional `within*` properties narrow the window: the value must also be after
 * (now - the specified duration), e.g. `withinDays = 7` requires the value within the last 7 days.
 *
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = PastConstraint::class, validatedBy = [PastValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Past(
	val withinSeconds: Long = 0,
	val withinMinutes: Long = 0,
	val withinHours: Long = 0,
	val withinDays: Long = 0,
	val withinWeeks: Long = 0,
	val withinMonths: Long = 0,
	val withinYears: Long = 0,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)