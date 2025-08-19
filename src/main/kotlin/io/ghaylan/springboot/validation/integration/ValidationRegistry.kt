package io.ghaylan.springboot.validation.integration

import io.ghaylan.springboot.validation.utils.ReflectionUtils
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.schema.RequestInputSchema
import io.ghaylan.springboot.validation.schema.RequestInputSchema.PropertySpec
import io.ghaylan.springboot.validation.schema.ValidationSchemaBuilder
import io.ghaylan.springboot.validation.schema.ValidatorBuilder
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Central registry and factory for managing constraint validators and validation schemas
 * within the Spring application context.
 *
 * This Spring-managed component orchestrates:
 *
 * 1. **Discovery and instantiation of constraint validators:**
 *    - Scans the classpath for all annotations marked with [Constraint].
 *    - For each constraint annotation, loads and instantiates its associated
 *      [ConstraintValidator] classes as declared via `validatedBy`.
 *    - Supports Kotlin `object` singletons, Spring-managed beans, autowired instances,
 *      or fallback no-arg constructor instantiation.
 *
 * 2. **Caching and retrieval of validation schemas:**
 *    - Pre-builds static validation schemas for all Spring controller endpoints annotated
 *      with validation metadata, caching them for efficient runtime lookup by a generated id.
 *    - Supports generating and caching dynamic validation schemas for arbitrary classes on demand,
 *      keyed by their runtime type information ([TypeInfo]).
 *
 * 3. **Group-aware schema filtering:**
 *    - Allows selective application of validation constraints based on validation groups,
 *      facilitating flexible and conditional validation rules akin to the standard Spring Validation framework.
 *
 * ---
 *
 * ### Typical usage examples:
 * ```kotlin
 * // Injected by Spring into controllers or services
 * val registry: ValidationRegistry = ...
 *
 * // Retrieve static schema for a specific endpoint
 * val schema = registry.getSchemaByRequest("xxx")
 *
 * // Generate or retrieve dynamic schema for a DTO class with validation groups
 * val (typeInfo, dynamicSchema) = registry.resolveSchemaByClass(MyDto::class.java)
 * ```
 */
open class ValidationRegistry : ApplicationListener<ContextRefreshedEvent>
{
    /**
     * Registry mapping constraint annotation types to their supported value types and validator instances.
     * Built once at startup by scanning the classpath.
     */
    private val validators = HashMap<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>()

    /**
     * List of static validation schemas for Spring MVC endpoints, keyed by HTTP method and URI pattern.
     * Used for quick lookup of endpoint validation metadata at runtime.
     */
    val staticSchemas = mutableMapOf<String, RequestInputSchema>()

    /**
     * Cache of dynamic validation schemas for arbitrary classes, keyed by the class type.
     * Schemas include property specs and constraints for runtime validation of DTOs or custom objects.
     */
    private val dynamicSchemas = ConcurrentHashMap<Class<*>, Map<String, PropertySpec>>()



    /**
     * Initializes the validator registry and pre-builds static validation schemas for all
     * annotated Spring MVC controller endpoints.
     *
     * This method is automatically invoked by Spring after the component's construction,
     * leveraging [ValidatorBuilder] and [ValidationSchemaBuilder] to populate internal caches.
     */
    override fun onApplicationEvent(event: ContextRefreshedEvent)
    {
        val appContext = event.applicationContext

        ValidatorBuilder.buildValidators(appContext).forEach {
            validators[it.key] = it.value
        }

        ValidationSchemaBuilder.generateStaticSchemas(appContext, validators).forEach {
            staticSchemas[it.key] = it.value
        }
    }


    /**
     * Retrieves the **static validation schema** for a given class method.
     *
     * This method is intended to be used for validating HTTP requests against schemas
     * pre-built at application startup for Spring MVC controller endpoints.
     *
     * @param id The unique identifier for the class method
     * @return The corresponding [RequestInputSchema] if a match is found; otherwise, `null`.
     */
    fun getSchemaByRequest(id : String) : RequestInputSchema?
    {
        return staticSchemas[id]
    }


    /**
     * Retrieves or dynamically generates a validation schema for an arbitrary class type at runtime.
     *
     * This supports validation of DTOs or custom objects outside the context of HTTP endpoints.
     * The generated schema includes property metadata and constraint validators filtered by
     * the current validation groups.
     *
     * Schemas are cached in [dynamicSchemas] for subsequent reuse.
     *
     * @param clazz The Java [Class] for which to generate or retrieve the validation schema.
     * @return A [Pair] containing:
     *   - The resolved [TypeInfo] for the class.
     *   - A map from property names to their detailed [PropertySpec], including constraints and nested properties.
     *
     * @throws IllegalStateException If no schema could be generated or if the schema is empty or invalid.
     */
    fun resolveSchemaByClass(
        clazz : Class<*>,
    ) : Pair<TypeInfo, Map<String, PropertySpec>>
    {
        val typeInfo = ReflectionUtils.infoFromClass(clazz)
        val type = typeInfo.resolveType.java

        val schema = dynamicSchemas.computeIfAbsent(type) {

            val specs = ValidationSchemaBuilder.generateSchemaForType(
                rootClass = type,
                allValidators = validators
            ) ?: error("Could not create validation schema for type ${type.name}")

            require(specs.second.isNotEmpty()) {
                "Object specs must contain at least one field for type ${type.name}"
            }

            require(specs.second.any { it.value.constraints.isNotEmpty() }) {
                "Object specs must contain at least one constraint for type ${type.name}"
            }

            specs.second
        }

        return typeInfo to schema
    }
}