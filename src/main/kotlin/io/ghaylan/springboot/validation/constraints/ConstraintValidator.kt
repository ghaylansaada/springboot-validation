package io.ghaylan.springboot.validation.constraints

import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.errors.ApiError
import io.ghaylan.springboot.validation.ext.extractLanguage
import io.ghaylan.springboot.validation.ext.normalizeLanguageTag

/**
 * Base abstract class for all validation constraints.
 *
 * A [ConstraintValidator] is responsible for applying the validation logic
 * for a specific constraint metadata type against a given input value within
 * a validation context. It produces a domain-level validation error if validation fails.
 *
 * ### Core responsibilities:
 * - Perform validation based on provided [ConstraintMetadata] and input [Value].
 * - Respect validation groups for conditional validation activation.
 * - Generate localized error messages based on error codes and validation context language.
 *
 * @param Value The type of the value being validated.
 * @param Constraint The specific constraint metadata type associated with this validator.
 */
abstract class ConstraintValidator<Value, Constraint : ConstraintMetadata>
{

    /**
     * Executes validation on the provided [value] using the specified [constraint] metadata
     * and [context] describing the validation environment.
     *
     * This method manages group-based validation filtering and constructs
     * an [ApiError] with an appropriate localized message if validation fails.
     * Returns `null` if validation passes or is skipped due to group mismatch.
     *
     * @param value The value to validate, possibly null.
     * @param constraint The metadata describing validation rules to apply.
     * @param context The context providing field path, language, groups, and more.
     * @return An [ApiError] describing the failure, or `null` if valid or skipped.
     * @throws ClassCastException if [constraint] or [value] cannot be cast to expected types.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun runValidation(
        value: Any?,
        constraint: ConstraintMetadata,
        context: ValidationContext
    ) : ApiError?
    {
        if (!shouldValidate(constraint = constraint as Constraint, context = context)) return null

        val error = validate(
            value = value as Value?,
            constraint = constraint,
            context = context
        ) ?: return null

        return error.copy(
            path = context.fieldPath,
            location = context.location,
            message = getMessage(error = error, constraint = constraint, context = context))
    }


    /**
     * Selects the best localized error message to return based on the [error] code,
     * the available override messages in [constraint], and the current context language.
     *
     * The message selection priority is:
     * 1. Exact language-country match (e.g., "en-US")
     * 2. Language-only match (e.g., "en")
     * 3. English fallback ("en")
     * 4. Default message from the initial [error]
     *
     * Language tags are normalized by replacing underscores with hyphens
     * and compared case-insensitively.
     *
     * @param error Initial error object.
     * @param constraint The constraint metadata containing override messages.
     * @param context The validation context providing the target language.
     * @return The best matching localized message or the default error message.
     */
    private fun getMessage(
        error: ApiError,
        constraint: Constraint,
        context: ValidationContext
    ) : String
    {
        val contextLang = context.language.normalizeLanguageTag()
        val contextLangShort = contextLang.extractLanguage()
        var exactMatch: String? = null
        var partialMatch: String? = null
        var anyMatch: String? = null
        var englishFallback: String? = null

        for (msg in constraint.messages)
        {
            anyMatch = msg.text

            val msgLang = msg.language.normalizeLanguageTag()
            val msgLangShort = msgLang.extractLanguage()

            if (msgLang == contextLang) {
                // Exact match — highest priority
                exactMatch = msg.text
                break // can exit early since this is best possible match
            }

            if (partialMatch == null && msgLangShort == contextLangShort) {
                // Partial match — second priority
                partialMatch = msg.text
            }

            if (englishFallback == null && msgLang == "en") {
                // English fallback — third priority
                englishFallback = msg.text
            }
        }

        return exactMatch ?: partialMatch ?: englishFallback ?: anyMatch ?: error.message ?: ""
    }


    /**
     * Determines if the given [constraint] should be validated within the current [context]
     * by checking the intersection of validation groups.
     *
     * If the constraint defines no groups, validation is always performed.
     * If the context specifies no groups, no group-restricted constraints are validated.
     * Otherwise, validation occurs only if the constraint groups intersect with context groups.
     *
     * This method is optimized for performance by iterating over the smaller set.
     *
     * @param constraint The constraint metadata whose groups to check.
     * @param context The validation context providing active groups.
     * @return `true` if validation should proceed; `false` otherwise.
     */
    private fun shouldValidate(
        constraint : Constraint,
        context : ValidationContext
    ) : Boolean
    {
        // Fast exit: if constraint has no groups, always validate
        if (constraint.groups.isEmpty()) return true

        // Fast exit: if context has no groups, no match possible
        if (context.groups.isEmpty()) return false

        // Smallest set iteration for performance
        val (small, large) = if (constraint.groups.size <= context.groups.size) {
            constraint.groups to context.groups
        }
        else context.groups to constraint.groups

        // Check for intersection
        for (validationGroup in small) {
            if (validationGroup in large) return true
        }

        return false
    }


    /**
     * Abstract function that concrete validators must implement to perform the actual validation logic.
     *
     * @param value The input value to validate, possibly null.
     * @param constraint The constraint metadata describing validation rules.
     * @param context The validation context with additional info.
     * @return A pair of error code and default message if validation fails, or `null` if valid.
     */
    protected abstract suspend fun validate(
        value: Value?,
        constraint: Constraint,
        context: ValidationContext,
    ) : ApiError?


    /**
     * Helper function to retrieve a property value from the context's container object schema by property [name].
     *
     * @param name The name of the property to retrieve.
     * @param context The validation context containing the container object.
     * @return The value of the property if found; otherwise, `null`.
     */
    protected fun getPropertyValue(
        name : String,
        context : ValidationContext
    ) : Any?
    {
        return context.containerObject
            ?.schema[name]
            ?.accessor
            ?.getFromAny(context.containerObject.value)
    }
}