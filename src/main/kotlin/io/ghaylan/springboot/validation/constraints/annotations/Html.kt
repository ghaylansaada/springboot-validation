package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.html.HtmlConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.html.HtmlValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence contains only allowed HTML content.
 *
 * ### Description
 * The `@Html` annotation ensures that the annotated string or character sequence
 * contains HTML markup restricted to a configurable set of allowed tags, attributes,
 * and attribute protocols. This is useful for validating user inputs that permit
 * limited HTML for formatting, such as rich text fields, comments, or descriptions,
 * while preventing injection of unsafe or disallowed HTML elements.
 *
 * The validation leverages a dynamically constructed [org.jsoup.safety.Safelist]
 * to sanitize and verify the input HTML content.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null` or blank.
 * - Only the specified HTML tags are allowed.
 * - Only the specified attributes on specific tags are allowed.
 * - Only the specified protocols on specific tag attributes are allowed.
 * - The HTML content must not contain disallowed tags, attributes, or protocols.
 * - Validation fails if the cleaned/sanitized HTML differs from the original input,
 *   indicating disallowed content was present.
 *
 * ### Example Usage
 * ```kotlin
 * @Html
 * val description: String
 * // ✅ description can contain only <b>, <i>, <u>, <p>, <ul>, <li>, <a>, <span>, and <strong> tags
 * // and <a> tags may have only "href" attribute with "http" or "https" protocols.
 *
 * -----
 *
 * @Html(
 *   allowedTags = ["b", "i", "em", "strong", "a"],
 *   allowedAttrs = ["a:href", "a:title"],
 *   allowedProtocols = ["http", "https"])
 * val comment: String
 * // ✅ comment allows <a> tags with "href" and "title" attributes,
 * // but "href" protocols must be http or https.
 * ```
 *
 * ### Error Codes Used in Validators
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.HTML_TAG_VIOLATION]
 *   Triggered when an HTML tag is not in the allowed list.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.HTML_ATTRIBUTE_VIOLATION]
 *   Triggered when an attribute on a tag is not allowed.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.HTML_PROTOCOL_VIOLATION]
 *   Triggered when a protocol in an attribute value is not allowed.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.HTML_VALUE_VIOLATION]
 *   Also triggered when the sanitized HTML differs from the input, indicating disallowed content.
 *
 * @property allowedTags The list of allowed HTML tags. Default includes common formatting tags like "b", "i", "u", "p", "ul", "li", "a", "span", and "strong".
 * @property allowedAttrs The list of allowed tag attributes in the form `"tag:attribute"`. For example, `"a:href"` allows the `href` attribute on `<a>` tags.
 * @property allowedProtocols The list of allowed protocols for tag attributes, in the form `"tag:attribute:protocol1,protocol2"`.
 *   For example, `"a:href:http,https"` restricts `<a href>` to `http` and `https` protocols only.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = HtmlConstraint::class, validatedBy = [HtmlValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Html(
    val allowedTags : Array<String> = ["b", "i", "u", "p", "ul", "li", "a", "span", "strong"],
    val allowedAttrs : Array<String> = ["a:href"],
    val allowedProtocols : Array<String> = ["http", "https"],
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])