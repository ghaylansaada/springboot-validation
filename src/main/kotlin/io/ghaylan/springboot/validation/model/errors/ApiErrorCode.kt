package io.ghaylan.springboot.validation.model.errors

/**
 * Standardized enumeration of error codes representing distinct categories of input validation failures.
 *
 * These error codes classify validation errors encountered during request processing,
 * enabling uniform error reporting, handling, and client feedback across the application.
 */
enum class ApiErrorCode {

    // --- Presence and Equality Violations ---
    REQUIRED_VIOLATION,
    EQUALITY_VIOLATION,
    INEQUALITY_VIOLATION,

    // --- Range and Value Constraints ---
    GREATER_THAN_VIOLATION,
    LESS_THAN_VIOLATION,
    MIN_VALUE_VIOLATION,
    MAX_VALUE_VIOLATION,
    ALLOWED_VALUE_VIOLATION,
    DISALLOWED_VALUE_VIOLATION,
    DIVISIBILITY_VIOLATION,
    MULTIPLICITY_VIOLATION,

    // --- Geographical and Temporal Constraints ---
    LATITUDE_VIOLATION,
    LONGITUDE_VIOLATION,
    PAST_VIOLATION,
    FUTURE_VIOLATION,
    DAY_OF_WEEK_VIOLATION,

    // --- Format and Pattern Violations ---
    BASE64_VIOLATION,
    ISO_COUNTRY_CODE_VIOLATION,
    ISO_CURRENCY_CODE_VIOLATION,
    ISO_LANGUAGE_CODE_VIOLATION,
    CREDIT_CARD_PATTERN_VIOLATION,

    EMAIL_FORMAT_VIOLATION,
    UUID_PATTERN_VIOLATION,

    URL_VIOLATION,
    URL_HTTPS_REQUIRED_VIOLATION,
    URL_QUERY_PARAMS_NOT_ALLOWED_VIOLATION,
    URL_EXTENSION_VIOLATION,
    URL_TYPE_VIOLATION,

    HTML_TAG_VIOLATION,
    HTML_VALUE_VIOLATION,
    HTML_ATTRIBUTE_VIOLATION,
    HTML_PROTOCOL_VIOLATION,

    STRING_OCCURRENCES_VIOLATION,
    PATTERN_VIOLATION,
    PHONE_FORMAT_VIOLATION,
    PHONE_TYPE_VIOLATION,
    PHONE_COUNTRY_VIOLATION,

    NBR_FORMAT_VIOLATION,
    IBAN_FORMAT_VIOLATION,
    HEX_COLOR_CODE_FORMAT_VIOLATION,
    ENUM_FORMAT_VIOLATION,

    // --- Password and Security Constraints ---
    PASSWORD_LENGTH_VIOLATION,
    PASSWORD_UPPERCASE_VIOLATION,
    PASSWORD_LOWERCASE_VIOLATION,
    PASSWORD_DIGIT_VIOLATION,
    PASSWORD_SPECIAL_CHAR_VIOLATION,
    PASSWORD_ENTROPY_VIOLATION,

    // --- Collection and Object Structure Constraints ---
    STRING_LENGTH_VIOLATION,
    OBJECT_SIZE_VIOLATION,
    ARRAY_SIZE_VIOLATION,
    DISTINCT_VALUE_VIOLATION,

    // --- Dependency and Consistency Issues ---
    DEPENDENCY_TYPE_VIOLATION
}