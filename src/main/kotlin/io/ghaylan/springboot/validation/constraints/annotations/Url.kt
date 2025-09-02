package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.validators.string.url.UrlConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.url.UrlValidator
import io.ghaylan.springboot.validation.groups.DefaultGroup
import kotlin.reflect.KClass

/**
 * Validates that the annotated character sequence represents a syntactically valid and semantically constrained URL.
 *
 * ### Description
 * The `@Url` annotation ensures that the annotated value (or each element in a collection)
 * conforms to the syntax of a valid URL and optionally matches additional constraints such as protocol,
 * query parameter rules, and file extensions depending on the specified [type].
 *
 * This annotation is particularly useful for validating web links, downloadable media,
 * file resources, and user-submitted URLs within REST APIs or web forms.
 *
 * ### Supported Types
 * - `String`
 * - `CharSequence`
 * - Arrays or collections of the above types (each element is validated individually)
 *
 * ### Validation Rules
 * - Validation is skipped if the value is `null`.
 * - Validation fails if the value cannot be parsed as a URI using [java.net.URI].
 * - If [requireHttps] is `true`, the URL must use the `https` scheme.
 * - If [allowQueryParams] is `false`, the URL must not contain query parameters.
 * - If [type] is [UrlType.WEBSITE], the URL must include a valid domain (host).
 * - If [type] is a media-related type (e.g., [UrlType.IMAGE], [UrlType.FILE]):
 *   - The URL must end with a valid file extension from [allowedExtensions], or from the default list associated with the type.
 *
 * ### Example Usage
 * ```kotlin
 * @Url
 * val profileUrl: String
 * // ✅ Any valid URL is accepted (generic mode)
 *
 * -----
 *
 * @Url(type = UrlType.IMAGE)
 * val avatarLink: String
 * // ✅ Must be a valid URL ending in image formats like .jpg or .png
 *
 * -----
 *
 * @Url(type = UrlType.FILE, requireHttps = true, allowQueryParams = false)
 * val resourceUrl: String
 * // ✅ Must be HTTPS, without query parameters, and end with a file extension like .pdf or .zip
 *
 * -----
 *
 * @Url(type = UrlType.WEBSITE)
 * val homepage: String
 * // ✅ Must contain a domain (host), e.g., "https://example.com"
 *
 * -----
 *
 * @Url(type = UrlType.VIDEO, allowedExtensions = ["mp4", "webm"])
 * val playlist: List<String>
 * // ✅ Each element must be a valid video URL ending with .mp4 or .webm
 * ```
 *
 * ### Error Codes Used in Validator
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.URL_VIOLATION]
 *   Triggered when the value is not a valid URL.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.URL_HTTPS_REQUIRED_VIOLATION]
 *   Triggered when HTTPS is required but the value does not start with `https://`.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.URL_QUERY_PARAMS_NOT_ALLOWED_VIOLATION]
 *   Triggered when query parameters are not allowed but the value contains them.
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.URL_TYPE_VIOLATION]
 *   Triggered when the URL does not satisfy type-specific rules (e.g., missing domain for [UrlType.WEBSITE]).
 * - [io.ghaylan.springboot.validation.model.errors.ApiErrorCode.URL_EXTENSION_VIOLATION]
 *   Triggered when the URL does not end with an allowed file extension.
 *
 * @property type Defines the expected type of the URL. Certain types (e.g., [UrlType.IMAGE], [UrlType.FILE]) require specific file extensions or structural characteristics.
 * @property requireHttps If `true`, the URL must use the `https://` scheme.
 * @property allowQueryParams If `false`, the URL must not include query parameters.
 * @property allowedExtensions Optional list of allowed file extensions. If not provided, the default list defined in the [type] will be used.
 * @property groups Specifies the validation groups this constraint belongs to.
 *                  Validation groups enable selective validation by grouping constraints,
 *                  allowing the constraint to be applied only during validation runs targeting those groups.
 *                  Defaults to the `DefaultGroup` if none are specified.
 * @property messages Optional array of [Message] annotations for overriding
 *                   default error messages with localized, error-code-specific messages.
 */
@MustBeDocumented
@Constraint(metadata = UrlConstraint::class, validatedBy = [UrlValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Url(
    val type : UrlType = UrlType.GENERIC,
    val requireHttps : Boolean = false,
    val allowQueryParams : Boolean = true,
    val allowedExtensions : Array<String> = [],
    val groups: Array<KClass<*>> = [DefaultGroup::class],
    val messages : Array<Message> = [])
{

	/**
	 * Defines types of URL categories for validation context.
	 *
	 * @property isMedia Indicates whether this type typically represents downloadable or embedded media.
	 * @property extensions Default list of file extensions typically associated with this category.
	 */
	enum class UrlType(val isMedia : Boolean, val extensions : Array<String>)
	{
		GENERIC(false, emptyArray()),  // Any valid URL
		WEBSITE(false, emptyArray()),  // Must start with HTTP(S) and contain a domain
		FILE(true, arrayOf("pdf", "zip", "rar", "tar", "exe", "doc", "docx", "ppt", "pptx", "xls", "xlsx")),
		IMAGE(true, arrayOf("jpg", "jpeg", "png", "gif", "webp", "svg", "bmp", "tiff", "ico")),
		VIDEO(true, arrayOf("mp4", "avi", "mov", "mkv", "flv", "wmv", "webm", "mpeg")),
		AUDIO(true, arrayOf("mp3", "wav", "ogg", "flac", "aac", "wma", "m4a", "opus"))
	}
}