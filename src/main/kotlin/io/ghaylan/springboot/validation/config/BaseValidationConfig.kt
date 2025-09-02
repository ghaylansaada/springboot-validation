package io.ghaylan.springboot.validation.config

import io.ghaylan.springboot.validation.engine.ValidatorEngine
import io.ghaylan.springboot.validation.integration.ValidationAspect
import io.ghaylan.springboot.validation.integration.ValidationRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.web.filter.reactive.ServerWebExchangeContextFilter

/**
 * Auto-configuration for the validation framework.
 *
 * Provides default beans for validation integration, including:
 * - [ServerWebExchangeContextFilter] for reactive web context access.
 * - [ValidationRegistry] to hold and manage validation definitions.
 * - [ValidatorEngine] for executing validation logic.
 * - [ValidationAspect] for applying validation via AOP.
 *
 * All beans are only created if no other bean of the same type is present
 * in the Spring context, allowing easy customization by the application.
 */
@AutoConfiguration
open class BaseValidationConfig
{
    /**
     * Provides a [ServerWebExchangeContextFilter] bean to make the current
     * reactive server exchange available in the context.
     */
    @Bean
    @ConditionalOnMissingBean
    open fun exchangeContextWebFilter() : ServerWebExchangeContextFilter = ServerWebExchangeContextFilter()

    /**
     * Creates the [ValidationRegistry] bean which acts as a container
     * for all registered validations.
     */
    @Bean
    @ConditionalOnMissingBean
    open fun validationRegistry() = ValidationRegistry()

    /**
     * Creates the [ValidatorEngine] bean which executes validations
     * using the [ValidationRegistry].
     *
     * @param validationRegistry The registry containing all validations.
     */
    @Bean
    @ConditionalOnMissingBean
    open fun validatorEngine(
        validationRegistry: ValidationRegistry
    ) = ValidatorEngine(validationRegistry)

    /**
     * Creates the [ValidationAspect] bean which applies validations
     * via Spring AOP on annotated methods.
     *
     * @param validatorEngine The engine used to perform validations.
     */
    @Bean
    @ConditionalOnMissingBean
    open fun validationAspect(
        validatorEngine: ValidatorEngine
    ) = ValidationAspect(validatorEngine)
}