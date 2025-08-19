package io.ghaylan.springboot.validation.schema

import io.ghaylan.springboot.validation.utils.ReflectionUtils
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.utils.SpringBootUtils
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import kotlin.collections.set
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.superclasses

/**
 * Utility responsible for discovering, instantiating, and registering all available
 * [ConstraintValidator] implementations within a Spring Boot application.
 *
 * This builder performs comprehensive scanning and resolution of validator classes by:
 * - Locating all annotations meta-annotated with [Constraint].
 * - Extracting their declared validator classes from the `validatedBy` attribute.
 * - Instantiating validators through Kotlin singleton objects, Spring beans, autowiring, or no-arg constructors.
 * - Analyzing generic parameters of validators to associate each with its corresponding constraint type and supported value type.
 * - Building a nested registry mapping constraint annotation types to compatible value types and their respective validators.
 *
 * ## Purpose and Use Case:
 * The framework calls this component at application startup to build a fast-access registry
 * of all constraint validators available for runtime validation. This enables efficient,
 * type-safe, and dynamic matching of constraints to validators based on the actual types
 * of fields or method parameters being validated.
 *
 * It supports validators declared as Kotlin `object`s, Spring-managed beans, or simple POJOs,
 * ensuring only one instance of each validator class is created and reused across the application.
 *
 * ## Output Registry Structure:
 * The resulting registry has the type:
 * ```kotlin
 * Map<ConstraintAnnotationType, Map<ValueTypeInfo, ConstraintValidatorInstance>>
 * ```
 * Where:
 * - **ConstraintAnnotationType** is a KClass representing the annotation that declares the constraint (a subtype of [ConstraintMetadata]).
 * - **ValueTypeInfo** is a detailed type descriptor ([TypeInfo]) of the Java/Kotlin type the validator supports.
 * - **ConstraintValidatorInstance** is the instantiated validator handling that type.
 *
 * This structure allows rapid and precise lookup of validators during validation execution.
 *
 * ## Typical Invocation:
 * ```kotlin
 * val validators = ValidatorBuilder.buildValidators(applicationContext)
 * ```
 *
 * This method should be invoked once during the application lifecycle, typically at startup,
 * and the result cached for use by the validation engine.
 */
object ValidatorBuilder
{

    /**
     * Discovers and instantiates all available [ConstraintValidator] implementations in the application.
     *
     * This function scans the classpath for annotations marked with `@Constraint`, then reads their associated
     * `validatedBy` validator classes. It resolves each validator’s supported value type and constraint type
     * by analyzing its generic parameters, then instantiates each validator using the following strategies:
     *
     * 1. Kotlin `object` singleton
     * 2. Spring-managed bean (singleton or scoped)
     * 3. Autowiring via [AutowireCapableBeanFactory]
     * 4. Fallback to a no-arg constructor
     *
     * Although validators may not be true singletons, the framework ensures that each validator class is only
     * instantiated once and reused for subsequent validations.
     *
     * The result is a nested map that allows fast lookup of the appropriate validator based on the constraint
     * annotation type and the type of the field/property being validated.
     *
     * @param appContext The Spring [ApplicationContext], used to access beans, autowiring facilities,
     *                   and base package metadata.
     *
     * @return A nested map of discovered validators:
     *   - **Outer key**: [KClass] of the constraint annotation type (subtype of [ConstraintMetadata])
     *   - **Inner key**: [TypeInfo] representing the supported input type handled by the validator
     *   - **Value**: The corresponding [ConstraintValidator] instance capable of handling that value type
     *
     * @throws IllegalStateException If a validator cannot be instantiated or is incorrectly typed
     */
    fun buildValidators(
        appContext : ApplicationContext,
    ) : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>>
    {
        val validators = HashMap<KClass<out ConstraintMetadata>, HashMap<TypeInfo, ConstraintValidator<*,*>>>()

        val beanFactory = appContext.autowireCapableBeanFactory

        val allPackages = SpringBootUtils.resolveBasePackages(appContext, beanFactory)

        // Scanner that looks for annotations marked with @Constraint
        val scanner = object : ClassPathScanningCandidateComponentProvider(false)
        {
            // allow scanning for annotations themselves
            override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean
            {
                return beanDefinition.metadata.isAnnotated(Constraint::class.java.name) && beanDefinition.metadata.isAnnotation
            }
        }

        scanner.addIncludeFilter(AnnotationTypeFilter(Constraint::class.java))

        val annotationDefs = allPackages.flatMap { scanner.findCandidateComponents(it) }

        for (annotationDef in annotationDefs)
        {
            val validatorsClasses = resolveValidatorsClass(annotationDef.beanClassName) ?: continue

            for (validatorClass in validatorsClasses)
            {
                val (constraintType, valueType) = resolveConstraintAndValueType(validatorClass)
                val valueTypeMap = validators.getOrPut(constraintType) { hashMapOf() }
                valueTypeMap[valueType] = resolveValidatorInstance(appContext, beanFactory, validatorClass)
            }
        }

        return validators
    }


    /**
     * Resolves the validator classes associated with a constraint annotation.
     *
     * This reads the `validatedBy` property from the `@Constraint` meta-annotation on an annotation class.
     *
     * @param constraintName Fully qualified class name of an annotation (as returned by Spring's scanner).
     * @return An array of [ConstraintValidator] classes if the annotation is valid, or `null` otherwise.
     */
    @Suppress("UNCHECKED_CAST")
    private fun resolveValidatorsClass(
        constraintName: String?
    ): Array<KClass<out ConstraintValidator<out Any, out ConstraintMetadata>>>?
    {
        return Class.forName(constraintName ?: return null)
            .takeIf { it.isAnnotation }
            ?.let { it as? Class<out Annotation>? }
            ?.getAnnotation(Constraint::class.java)
            ?.validatedBy
    }


    /**
     * Resolves and instantiates a [ConstraintValidator] implementation using a flexible fallback strategy.
     *
     * This method is designed to support a wide range of validator declarations while ensuring that,
     * regardless of the instantiation mechanism, **only one instance per validator class is ever created and reused**.
     * This allows the system to simulate singleton-like behavior even if the underlying class is not a true singleton.
     *
     * The resolution follows these ordered strategies:
     *
     * 1. **Kotlin `object` singleton** — Returns the singleton instance if the validator is declared using Kotlin's `object` keyword.
     * 2. **Spring-managed bean** — Returns an existing Spring bean instance, if one is registered in the context (regardless of its scope).
     * 3. **Autowired instantiation** — Uses [AutowireCapableBeanFactory] to construct and inject dependencies into a new validator instance.
     * 4. **Manual no-arg constructor fallback** — Invokes a no-argument constructor via reflection if all other strategies fail.
     *
     * Although validators instantiated through strategy (3) or (4) are not inherently singletons,
     * the framework is responsible for **caching and reusing** these instances to ensure consistent reuse across validation operations.
     *
     * @param appContext The Spring [ApplicationContext], used for retrieving or constructing beans.
     * @param beanFactory The [AutowireCapableBeanFactory] to programmatically create and autowire new validator instances.
     * @param validatorKClass The [KClass] of the [ConstraintValidator] to instantiate.
     * @return A fully constructed and reusable validator instance for the given type.
     *
     * @throws IllegalStateException If the validator cannot be instantiated using any of the defined strategies.
     */
    private fun resolveValidatorInstance(
        appContext : ApplicationContext,
        beanFactory : AutowireCapableBeanFactory,
        validatorKClass: KClass<out ConstraintValidator<out Any, out ConstraintMetadata>>
    ) : ConstraintValidator<out Any, out ConstraintMetadata>
    {
        // 1. Kotlin object?
        validatorKClass.objectInstance?.let { return it }

        // 2. A Spring-managed bean already exists (singleton / scoped)
        appContext.getBeanProvider(validatorKClass.java).ifAvailable?.let { return it }

        // 3. Create (autowire) a new instance
        runCatching {
            beanFactory.createBean(validatorKClass.java)
        }.getOrNull()?.let { return it }

        // 4. Fallback: bare no-arg constructor if autowire failed
        return validatorKClass.constructors.firstOrNull {
            it.parameters.isEmpty()
        }?.call() ?: error("Cannot instantiate ${validatorKClass.qualifiedName}: no bean and no no-arg constructor")
    }


    /**
     * Resolves both the constraint annotation type and the supported value type for a given [ConstraintValidator] class.
     *
     * This function inspects the generic parameters of the [ConstraintValidator] interface that the class implements,
     * even if the implementation is inherited indirectly.
     *
     * It returns:
     *  - The constraint annotation type (i.e., a subtype of [ConstraintMetadata])
     *  - The supported value type (wrapped as a [TypeInfo] for introspection and comparison purposes)
     *
     * This resolution is used to match validator classes to annotated fields or properties at runtime.
     *
     * @param validatorClass The validator class implementing or inheriting [ConstraintValidator]
     * @return A [Pair] where:
     *   - `first` is the [KClass] of the constraint annotation type ([ConstraintMetadata] subtype)
     *   - `second` is the [TypeInfo] describing the supported value type (e.g., `Int`, `String`, `List<String>`, etc.)
     *
     * @throws IllegalArgumentException If the validator does not explicitly declare valid generic type arguments.
     * @throws IllegalStateException If no [ConstraintValidator] supertype is found in the class hierarchy.
     */
    private fun resolveConstraintAndValueType(
        validatorClass: KClass<out ConstraintValidator<*, *>>
    ) : Pair<KClass<out ConstraintMetadata>, TypeInfo>
    {
        val superType = findConstraintValidatorSuperType(validatorClass)
            ?: error("Class ${validatorClass.simpleName} does not inherit from ConstraintValidator")

        val args = superType.arguments

        require(args.size == 2) { "Expected 2 generic arguments for ConstraintValidator" }

        @Suppress("UNCHECKED_CAST")
        val constraintType = args[1].type?.classifier as? KClass<out ConstraintMetadata>?
            ?: error("Second generic argument must be a BaseConstraintMetadata subtype")

        val valueTypeArg = superType.arguments
            .getOrNull(0)
            ?.type
            ?: error("Missing value type in generic arguments")

        return constraintType to ReflectionUtils.infoFromKType(valueTypeArg)
    }


    /**
     * Recursively searches the class hierarchy of the given [validatorClass] to locate its inherited
     * [ConstraintValidator] supertype with explicit generic parameters.
     *
     * This is required because the validator may implement [ConstraintValidator] indirectly,
     * for example through abstract base classes or interfaces.
     *
     * The function traverses the immediate supertypes and then walks up the inheritance tree
     * via [KClass.superclasses] until it finds the matching generic [KType].
     *
     * @param validatorClass The validator class to inspect.
     * @return The [KType] of the matching `ConstraintValidator<*, *>` interface, or `null` if not found.
     */
    private fun findConstraintValidatorSuperType(
        validatorClass: KClass<*>
    ): KType?
    {
        return validatorClass.supertypes.firstOrNull { it.classifier == ConstraintValidator::class }
            ?: validatorClass.superclasses.firstNotNullOfOrNull { findConstraintValidatorSuperType(it) }
    }
}