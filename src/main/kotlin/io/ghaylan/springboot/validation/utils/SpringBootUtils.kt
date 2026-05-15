package io.ghaylan.springboot.validation.utils

import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment

/**
 * Resolves the base packages of the running Spring Boot application for classpath scanning.
 *
 * Tries in order: [AutoConfigurationPackages], `@SpringBootConfiguration` beans,
 * `spring.main.sources` property, then falls back to `"io.ghaylan.springboot"`.
 */
object SpringBootUtils {

    /**
     * Returns a set of package names for classpath scanning, guaranteed non-empty.
     */
    fun resolveBasePackages(
        context : ApplicationContext,
        beanFactory : AutowireCapableBeanFactory
    ): Set<String> {
        val detected = linkedSetOf("io.ghaylan.springboot")

        // 1. AutoConfigurationPackages
	    if (AutoConfigurationPackages.has(beanFactory)) {
            detected += AutoConfigurationPackages.get(beanFactory)
        }

        // 2. @SpringBootConfiguration beans
        detected += trySpringBootConfigurationBeans(context)

        // 3. spring.main.sources property
        detected += trySpringMainSources(context.environment)

        return detected
    }


    /** Extracts package names from all `@SpringBootConfiguration`-annotated beans, unwrapping proxies. */
    private fun trySpringBootConfigurationBeans(ctx: ApplicationContext): List<String> {
        return ctx.getBeansWithAnnotation<SpringBootConfiguration>().values
            .map { AopUtils.getTargetClass(it) }
            .map { it.packageName }
            .distinct()
    }


    /** Reads comma-separated class names from `spring.main.sources` and returns their package names. */
    private fun trySpringMainSources(env: Environment): List<String> {
        val raw = env.getProperty("spring.main.sources") ?: return emptyList()
        return raw.split(',')
            .mapNotNull { str -> str.trim().takeIf { it.isNotEmpty() } }
            .mapNotNull { runCatching { Class.forName(it).`package`.name }.getOrNull() }
            .distinct()
    }
}