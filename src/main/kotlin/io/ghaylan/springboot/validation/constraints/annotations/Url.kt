package io.ghaylan.springboot.validation.constraints.annotations

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.validators.string.url.UrlConstraint
import io.ghaylan.springboot.validation.constraints.validators.string.url.UrlValidator
import io.ghaylan.springboot.validation.groups.OnDefault
import kotlin.reflect.KClass

/**
 * Validates that the annotated value is a parseable URI that satisfies optional scheme, query, and extension constraints.
 * Use [type] to enforce media-type or website-specific rules (e.g., allowed file extensions for [UrlType.IMAGE]).
 *
 * @property type Category of URL; media types ([UrlType.IMAGE], [UrlType.FILE], etc.) also enforce file extensions.
 * @property requireHttps Require the `https://` scheme.
 * @property allowQueryParams When `false`, the URL must not contain query parameters.
 * @property allowedExtensions Overrides the default extension list for the [type]; empty means use type defaults.
 * @property message Optional error message.
 */
@MustBeDocumented
@Constraint(metadata = UrlConstraint::class, validatedBy = [UrlValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class Url(
	val type: UrlType = UrlType.GENERIC,
	val requireHttps: Boolean = false,
	val allowQueryParams: Boolean = true,
	val allowedExtensions: Array<String> = [],
	val groups: Array<KClass<*>> = [OnDefault::class],
	val message: String = ""
) {
	
	/** URL category; media types also carry a default file-extension list. */
	enum class UrlType(
		val isMedia: Boolean,
		val extensions: Array<String>
	) {
		
		GENERIC(false, emptyArray()),  // Any valid URL
		WEBSITE(false, emptyArray()),  // Must start with HTTP(S) and contain a domain
		FILE(true, arrayOf("pdf", "zip", "rar", "tar", "exe", "doc", "docx", "ppt", "pptx", "xls", "xlsx")),
		IMAGE(true, arrayOf("jpg", "jpeg", "png", "gif", "webp", "svg", "bmp", "tiff", "ico")),
		VIDEO(true, arrayOf("mp4", "avi", "mov", "mkv", "flv", "wmv", "webm", "mpeg")),
		AUDIO(true, arrayOf("mp3", "wav", "ogg", "flac", "aac", "wma", "m4a", "opus"))
	}
}