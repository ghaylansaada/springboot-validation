package io.ghaylan.springboot.validation.constraints.message

/**
 * Defines a developer-provided, localized override for a validation error message.
 *
 * This annotation allows the developer to override the default error message associated with
 * a specific error code within a validator. It supports message localization through the
 * [lang] property, which accepts standard language tags (e.g., "en", "ar", "ar-TN").
 *
 *
 * ### Usage Notes
 * - Language tags should conform to standard [IETF BCP 47](https://tools.ietf.org/html/bcp47) format.
 * - Validators document the error codes they produce; developers can override one or multiple codes.
 *
 * @property lang The language tag for this message override, e.g., "en", "ar", "ar-TN". Defaults to "en".
 * @property text The custom error message text provided by the developer. Supports placeholders.
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class Message(val lang : String, val text: String)
