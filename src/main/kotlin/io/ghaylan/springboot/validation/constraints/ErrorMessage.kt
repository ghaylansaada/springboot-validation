package io.ghaylan.springboot.validation.constraints

/**
 * Defines a developer-provided, localized override for a validation error message.
 *
 * This annotation allows the developer to override the default error message associated with
 * a specific error code within a validator. It supports message localization through the
 * [language] property, which accepts standard language tags (e.g., "en", "ar", "ar-TN").
 *
 * When the validator triggers an error with a matching [errorCode], this message will replace
 * the default error message for the matching language. If [errorCode] is left empty, this message
 * will override the message for **all** error codes produced by the validator, effectively
 * applying a generic override.
 *
 * ### Usage Notes
 * - Language tags should conform to standard [IETF BCP 47](https://tools.ietf.org/html/bcp47) format.
 * - Validators document the error codes they produce; developers can override one or multiple codes.
 *
 * @property language The language tag for this message override, e.g., "en", "ar", "ar-TN". Defaults to "en".
 * @property text The custom error message text provided by the developer. Supports placeholders.
 * @property errorCode The specific error code whose default message this override replaces.
 *                     If empty, this message applies to all error codes from the validator.
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class ErrorMessage(
    val language : String,
    val text: String,
    val errorCode : String = "")
