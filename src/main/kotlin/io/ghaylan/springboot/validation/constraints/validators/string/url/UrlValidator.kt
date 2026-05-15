package io.ghaylan.springboot.validation.constraints.validators.string.url

import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.constraints.annotations.Url
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.model.errors.ApiErrorCode
import java.net.URI

/**
 * Validates `@Url` by parsing the value with [java.net.URI], then enforcing optional scheme,
 * query-param, domain, and file-extension rules based on the constraint configuration.
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


    override fun applicableErrorCodes(): Array<ApiErrorCode> = arrayOf(
        ApiErrorCode.URL_VIOLATION,
        ApiErrorCode.URL_HTTPS_REQUIRED_VIOLATION,
        ApiErrorCode.URL_QUERY_PARAMS_NOT_ALLOWED_VIOLATION,
        ApiErrorCode.URL_TYPE_VIOLATION,
        ApiErrorCode.URL_EXTENSION_VIOLATION)
}