package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.html.HtmlConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.html.HtmlValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated HTML string contains only the configured tags, attributes, and protocols.
 * Uses Jsoup's safelist sanitizer: validation fails if the sanitized output differs from the input.
 *
 * @property allowedTags HTML tags permitted in the input (default: common inline/block formatting tags).
 * @property allowedAttrs Allowed tag attributes as `"tag:attr"` pairs (e.g., `"a:href"`).
 * @property allowedProtocols Allowed protocols as `"tag:attr:proto1,proto2"` (e.g., `"a:href:http,https"`).
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = HtmlConstraint::class, validatedBy = [HtmlValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Html(
	val allowedTags: Array<String> = ["b", "i", "u", "p", "ul", "li", "a", "span", "strong"],
	val allowedAttrs: Array<String> = ["a:href"],
	val allowedProtocols: Array<String> = ["http", "https"],
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
)