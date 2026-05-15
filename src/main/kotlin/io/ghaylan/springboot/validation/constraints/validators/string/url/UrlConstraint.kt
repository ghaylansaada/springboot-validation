package io.ghaylan.springboot.validation.constraints.validators.string.url

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.annotations.Url.UrlType
import kotlin.reflect.KClass

/** Constraint metadata for [@Url][io.ghaylan.springboot.validation.constraints.annotations.Url]. */
data class UrlConstraint(
	val type: UrlType,
	val requireHttps: Boolean,
	val allowQueryParams: Boolean,
	val allowedExtensions: Set<String>,
	override val groups: Set<KClass<*>>,
	override val message: String
): ConstraintMetadata()