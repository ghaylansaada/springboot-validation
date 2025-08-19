package io.ghaylan.springboot.validation.model.errors

/**
 * DSL builder for creating structured error data maps.
 *
 * Used within [ApiErrorBuilder.data] to add contextual information
 * related to an error. Supports any key-value pairs, where the key is a string
 * and the value can be any object.
 *
 * Example usage with the DSL:
 * ```kotlin
 * val error = ApiErrorBuilder(ErrorLocation.BODY)
 *     .field("user.email")
 *     .code("INVALID_FORMAT")
 *     .data {
 *         add("attemptedValue", "not-an-email")
 *         add("maxLength", 50)
 *     }
 *     .build()
 *
 * // The resulting data map:
 * // data = mapOf(
 * //   "attemptedValue" to "not-an-email",
 * //   "maxLength" to 50
 * // )
 * ```
 */
class ErrorDataBuilder
{
    private val map = mutableMapOf<String, Any?>()


    /**
     * Adds a key-value pair to the error data.
     *
     * @param key The data key
     * @param value The corresponding value (any type)
     */
    fun add(key: String, value: Any?) { map[key] = value }


    /**
     * Builds and returns an immutable map of the configured error data.
     *
     * @return Immutable map of error data key-value pairs
     */
    fun build(): Map<String, Any?> = map.toMap()
}