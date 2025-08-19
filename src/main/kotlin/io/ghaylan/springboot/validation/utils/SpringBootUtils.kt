package io.ghaylan.springboot.validation.utils

import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import kotlin.collections.distinct
import kotlin.collections.map
import kotlin.collections.mapNotNull
import kotlin.collections.plusAssign
import kotlin.jvm.java
import kotlin.takeIf
import kotlin.text.isNotEmpty
import kotlin.text.split
import kotlin.text.trim

/**
 * Utility methods for resolving base packages in a Spring Boot application.
 *
 * This helper centralizes the logic to determine which packages should be used
 * for component scanning or other reflection-based operations when integrating
 * with a Spring Boot application.
 *
 * ### Resolution Strategy
 * The resolution follows a priority order:
 *
 * 1. **AutoConfigurationPackages**
 *    Uses [AutoConfigurationPackages] if the application has been registered with
 *    `@SpringBootApplication` or `@EnableAutoConfiguration`.
 *
 * 2. **@SpringBootConfiguration beans**
 *    If no packages were detected, looks for beans annotated with
 *    [SpringBootConfiguration] (typically the main application class), and
 *    extracts their package names.
 *
 * 3. **`spring.main.sources` property**
 *    If still unresolved, attempts to read the `spring.main.sources` property
 *    from the [Environment], which usually lists the application’s main classes.
 *
 * 4. **Fallback**
 *    Defaults to `"io.ghaylan.springboot"` if no packages are found by the
 *    previous steps.
 *
 * This order ensures compatibility with Spring Boot’s default behavior while
 * providing graceful fallbacks for custom integration scenarios.
 */
object SpringBootUtils
{

    /**
     * Resolves the base packages of the application using multiple strategies.
     *
     * @param context The active [ApplicationContext] of the Spring Boot application.
     * @param beanFactory The [AutowireCapableBeanFactory], required to query
     * [AutoConfigurationPackages].
     * @return A set of fully qualified package names to be used for
     * classpath scanning. Guaranteed to return at least one package.
     */
    fun resolveBasePackages(
        context : ApplicationContext,
        beanFactory : AutowireCapableBeanFactory
    ) : Set<String>
    {
        val detected = linkedSetOf("io.ghaylan.springboot")

        // 1. AutoConfigurationPackages
        if (AutoConfigurationPackages.has(beanFactory))
        {
            detected += AutoConfigurationPackages.get(beanFactory)
        }

        // 2. @SpringBootConfiguration beans
        detected += trySpringBootConfigurationBeans(context)

        // 3. spring.main.sources property
        detected += trySpringMainSources(context.environment)

        return detected
    }


    /**
     * Attempts to detect base packages by locating beans annotated with
     * [SpringBootConfiguration].
     *
     * Typical Spring Boot applications have a single main class annotated with
     * `@SpringBootApplication`, which is itself meta-annotated with
     * [SpringBootConfiguration]. This method retrieves all such beans, resolves
     * their actual target classes (unwrapping proxies if necessary), and returns
     * their package names.
     *
     * @param ctx The [ApplicationContext] to inspect for configuration beans.
     * @return A list of detected package names, or an empty list if no
     * configuration beans are found.
     */
    private fun trySpringBootConfigurationBeans(ctx : ApplicationContext): List<String>
    {
        val beans = ctx.getBeansWithAnnotation(SpringBootConfiguration::class.java).values

        return beans
            .map { AopUtils.getTargetClass(it) }
            .map { it.packageName }
            .distinct()
    }


    /**
     * Attempts to detect base packages by reading the `spring.main.sources`
     * property from the [Environment].
     *
     * The property typically contains a comma-separated list of fully qualified
     * class names representing the application’s entry points. Each class is
     * loaded reflectively, and its package is extracted.
     *
     * Invalid or missing entries are ignored gracefully.
     *
     * @param env The [Environment] from which to read the `spring.main.sources` property.
     * @return A list of detected package names, or an empty list if the property
     * is not set or contains no valid classes.
     */
    private fun trySpringMainSources(env: Environment): List<String>
    {
        val raw = env.getProperty("spring.main.sources") ?: return emptyList()
        return raw.split(',')
            .mapNotNull { str -> str.trim().takeIf { it.isNotEmpty() } }
            .mapNotNull { runCatching { Class.forName(it).`package`.name }.getOrNull() }
            .distinct()
    }
}