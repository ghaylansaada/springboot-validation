package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.enums.EnumConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.enums.EnumValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value conforms to Java/Kotlin enum naming conventions
 * (uppercase letters, digits not at start, underscores as separators).
 *
 * @property ignoreCase When `true`, case is ignored during character validation.
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = EnumConstraint::class, validatedBy = [EnumValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Enum(
	val ignoreCase: Boolean = false,
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)