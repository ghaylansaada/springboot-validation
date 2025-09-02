package io.ghaylan.springboot.validation.constraints.validators.string.url

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.constraints.annotations.Url
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import java.net.URI

/**
 * Validator implementation for the `@Url` annotation.
 *
 * ### Description
 * Validates that a given string is a syntactically valid and semantically appropriate URL based on configurable parameters.
 * Supports both standalone strings and collections or arrays of strings. Validation rules are enforced per element.
 * Relies on `java.net.URI` to check the structure of the URL, with additional domain and file extension logic applied.
 *
 * ### Supported types
 * - `CharSequence` (includes `String`)
 * - Arrays or collections of `CharSequence`
 *
 * ### Validation steps
 * 1. Parse the input string as a URI using `java.net.URI`.
 * 2. If `requireHttps` is true, the URI must start with the `https://` scheme.
 * 3. If `allowQueryParams` is false, the URI must not include query parameters.
 * 4. For `WEBSITE` type, the URI must include a valid host (domain).
 * 5. For media/file types (e.g., `IMAGE`, `AUDIO`, `FILE`), the path must end with a valid extension from `allowedExtensions`
 *    or the default list associated with the selected `UrlType`.
 */
object UrlValidator : ConstraintValidator<CharSequence, UrlConstraint>()
{

    override suspend fun validate(
        value: CharSequence?,
        constraint: UrlConstraint,
        context: ValidationContext
    ): ApiError?
    {
        value ?: return null

        // Try parsing the URL using java.net.URI
        val uri = runCatching {
            URI(value.toString())
        }.getOrNull() ?: return ApiError(code = ApiErrorCode.URL_VIOLATION, message = "Must be a valid URL")

        // Enforce HTTPS if required
        if (constraint.requireHttps && uri.scheme.lowercase() != "https")
        {
            return ApiError(code = ApiErrorCode.URL_HTTPS_REQUIRED_VIOLATION, message = "URL must use HTTPS protocol")
        }

        // Disallow query parameters if specified
        if (!constraint.allowQueryParams && !uri.query.isNullOrEmpty())
        {
            return ApiError(code = ApiErrorCode.URL_QUERY_PARAMS_NOT_ALLOWED_VIOLATION, message = "URL must not contain query parameters")
        }

        // For WEBSITE type: URL must have a host
        if (constraint.type == Url.UrlType.WEBSITE && uri.host.isNullOrBlank())
        {
            return ApiError(code = ApiErrorCode.URL_TYPE_VIOLATION, message = "URL must have a valid domain")
        }

        // For media types: validate file extension
        if (constraint.type.isMedia)
        {
            val extension = uri.path.substringAfterLast('.', "").lowercase()

            // Use user-defined extensions or fall back to defaults based on UrlType
            val typeExtensions = constraint.allowedExtensions.takeUnless {
                it.isEmpty()
            } ?: constraint.type.extensions.toSet()

            if (extension.isBlank() || !typeExtensions.contains(extension))
            {
                return ApiError(code = ApiErrorCode.URL_EXTENSION_VIOLATION, message = "URL must end with one of the following extensions: $typeExtensions")
            }
        }

        return null
    }
}