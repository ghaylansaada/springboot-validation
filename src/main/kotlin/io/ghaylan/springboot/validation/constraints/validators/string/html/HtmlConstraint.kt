package io.ghaylan.springboot.validation.constraints.validators.string.html

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import kotlin.reflect.KClass

/** Constraint metadata for [@Html][io.ghaylan.springboot.validation.constraints.annotations.Html]. */
data class HtmlConstraint(
	val allowedTags: Set<String>,
	val allowedAttrs: Set<String>,
	val allowedProtocols: Set<String>,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()
