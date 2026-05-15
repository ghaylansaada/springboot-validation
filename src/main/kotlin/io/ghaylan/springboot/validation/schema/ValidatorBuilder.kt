package io.ghaylan.springboot.validation.schema

import io.ghaylan.springboot.validation.constraints.Constraint
import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.ConstraintValidator
import io.ghaylan.springboot.validation.utils.ReflectionUtils
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import io.ghaylan.springboot.validation.utils.SpringBootUtils
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.superclasses

/**
 * Discovers, instantiates, and registers all [ConstraintValidator] implementations at startup.
 *
 * Scans for `@Constraint`-annotated annotations, resolves their validators (Kotlin object, Spring bean,
 * autowired, or no-arg constructor), and builds a lookup registry keyed by constraint type and value type.
 */
object ValidatorBuilder {

    /**
     * Scans the classpath for `@Constraint` annotations, instantiates their validators,
     * and returns a nested map: constraint type -> value type -> validator instance.
     */
    fun buildValidators(
        appContext : ApplicationContext,
    ) : Map<KClass<out ConstraintMetadata>, Map<TypeInfo, ConstraintValidator<*,*>>> {
        val validators = HashMap<KClass<out ConstraintMetadata>, HashMap<TypeInfo, ConstraintValidator<*,*>>>()

        val beanFactory = appContext.autowireCapableBeanFactory

        val allPackages = SpringBootUtils.resolveBasePackages(appContext, beanFactory)

        // Scanner that looks for annotations marked with @Constraint
        val scanner = object : ClassPathScanningCandidateComponentProvider(false) {
            // allow scanning for annotations themselves
            override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
                return beanDefinition.metadata.isAnnotated(Constraint::class.java.name) && beanDefinition.metadata.isAnnotation
            }
        }

        scanner.addIncludeFilter(AnnotationTypeFilter(Constraint::class.java))

        val annotationDefs = allPackages.flatMap { scanner.findCandidateComponents(it) }

        for (annotationDef in annotationDefs) {
            val validatorsClasses = resolveValidatorsClass(annotationDef.beanClassName) ?: continue

            for (validatorClass in validatorsClasses) {
                val (constraintType, valueType) = resolveConstraintAndValueType(validatorClass)
                val valueTypeMap = validators.getOrPut(constraintType) { hashMapOf() }
                valueTypeMap[valueType] = resolveValidatorInstance(appContext, beanFactory, validatorClass)
            }
        }

        return validators
    }


    /**
     * Reads the `validatedBy` property from a `@Constraint`-annotated annotation class.
     *
     * @param constraintName Fully qualified annotation class name (from Spring's component scanner).
     * @return Validator classes, or `null` if the class is not a valid constraint annotation.
     */
    @Suppress("UNCHECKED_CAST")
    private fun resolveValidatorsClass(
        constraintName: String?
    ): Array<KClass<out ConstraintValidator<out Any, out ConstraintMetadata>>>? {
        return Class.forName(constraintName ?: return null)
            .takeIf { it.isAnnotation }
            ?.let { it as? Class<out Annotation>? }
            ?.getAnnotation(Constraint::class.java)
            ?.validatedBy
    }


    /**
     * Instantiates a validator using: Kotlin object -> Spring bean -> autowire -> no-arg constructor.
     */
    private fun resolveValidatorInstance(
        appContext : ApplicationContext,
        beanFactory : AutowireCapableBeanFactory,
        validatorKClass: KClass<out ConstraintValidator<out Any, out ConstraintMetadata>>
    ) : ConstraintValidator<out Any, out ConstraintMetadata> {
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


    /** Extracts the constraint type and supported value type from a validator's generic parameters. */
    private fun resolveConstraintAndValueType(
        validatorClass: KClass<out ConstraintValidator<*, *>>
    ) : Pair<KClass<out ConstraintMetadata>, TypeInfo> {
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


    /** Walks the class hierarchy to find the `ConstraintValidator<*, *>` supertype. */
    private fun findConstraintValidatorSuperType(
        validatorClass: KClass<*>
    ): KType? {
        return validatorClass.supertypes.firstOrNull { it.classifier == ConstraintValidator::class }
            ?: validatorClass.superclasses.firstNotNullOfOrNull { findConstraintValidatorSuperType(it) }
    }
}