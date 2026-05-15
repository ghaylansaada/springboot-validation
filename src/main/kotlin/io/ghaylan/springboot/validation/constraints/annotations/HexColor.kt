package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.hexcolor.HexColorConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.hexcolor.HexColorValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is a CSS hex color in `#RGB` or `#RRGGBB` format
 * (case-insensitive hex digits after `#`).
 *
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = HexColorConstraint::class, validatedBy = [HexColorValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class HexColor(
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)