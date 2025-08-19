package io.ghaylan.springboot.validation.constraints.string.url

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ErrorMessageMetadata
import io.ghaylan.springboot.validation.constraints.string.url.Url.UrlType
import kotlin.reflect.KClass

data class UrlConstraint(
    val type : UrlType,
    val requireHttps : Boolean,
    val allowQueryParams : Boolean,
    val allowedExtensions : Set<String>,
    override val groups: Set<KClass<*>>,
    override val messages : Set<ErrorMessageMetadata>
) : ConstraintMetadata()