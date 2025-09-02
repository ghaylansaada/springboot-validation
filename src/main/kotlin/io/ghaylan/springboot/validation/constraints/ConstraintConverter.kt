package io.ghaylan.springboot.validation.constraints

import io.ghaylan.springboot.validation.constraints.message.Message
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmName

/**
 * Converts annotation-based validation definitions into pure metadata representations.
 *
 * `ConstraintConverter` provides a centralized and recursive mechanism for converting custom validation annotations
 * (e.g., `@ValueIn`, `@Required`, etc.) into corresponding [ConstraintMetadata] data classes. These metadata objects
 * are used by the validation engine to perform runtime checks based on declarative rules.
 *
 * This conversion is essential for systems that aim to decouple annotation parsing from validation logic,
 * allowing schema construction and validation to operate independently from raw annotations.
 *
 * ### Key Capabilities:
 * - **Constructor-Based Mapping**: Matches annotation properties with metadata class constructor parameters by name.
 * - **Smart Type Coercion**: Automatically converts `Array<KClass<*>>` to `Set<KClass<*>>` for validation groups and validators, and handles other known mismatches.
 * - **Nested Annotation Support**: Recursively resolves and converts annotations embedded within annotations.
 * - **Strong Runtime Validation**: Detects mismatched types and missing parameters, providing descriptive error messages to aid debugging.
 *
 * ### Example:
 * ```kotlin
 * @Constraint(
 *     metadata = ValueInConstraintMetadata::class,
 *     validatedBy = [ValueInValidator::class]
 * )
 * annotation class ValueIn(val values: Array<String>, val groups: Array<KClass<*>> = [], val by: Array<KClass<*>> = [])
 *
 * data class ValueInConstraintMetadata(
 *     val values: Set<String>,
 *     override val groups: Set<KClass<*>>,
 *     override val by: Set<KClass<*>>
 * ) : ConstraintMetadata
 *
 * val ann = instanceOf(ValueIn::class)
 * val metadata = ann.toMetadataSafely() // → ValueInConstraintMetadata
 * ```
 *
 * This mechanism is commonly used in schema parsing and validation frameworks to extract reusable metadata from annotations.
 */
object ConstraintConverter
{

    /**
     * Converts this annotation instance into a corresponding [ConstraintMetadata] object.
     *
     * This method inspects the annotation's `@Constraint` metadata, finds the linked metadata class,
     * and builds an instance of it using values extracted from the annotation.
     *
     * - Matches annotation property names with metadata constructor parameters.
     * - Converts known special cases (e.g., `groups` and `by` as `Array<KClass<*>>` to `Set<KClass<*>>`).
     * - Recursively resolves and converts nested annotations.
     * - Validates type compatibility at runtime and throws meaningful exceptions on mismatch.
     *
     * @return A constructed instance of the corresponding [ConstraintMetadata] subclass.
     * @throws IllegalStateException if the annotation is not marked with `@Constraint`, or if required mapping fails.
     */
    fun Annotation.convertToMetadata() : ConstraintMetadata
    {
        val constraintAnn = this.annotationClass.annotations
            .find { it is Constraint } as? Constraint
            ?: error("${this.annotationClass.jvmName} is not annotated with @Constraint")

        val metadataClass = constraintAnn.metadata
        val constructor = metadataClass.constructors.firstOrNull()
            ?: error("No constructor found for metadata class: ${metadataClass.simpleName}")

        val constructorParams = constructor.parameters.associateBy { it.name }

        val annotationProperties = this.annotationClass.members
            .filterIsInstance<KProperty1<Annotation, *>>()
            .associateBy { it.name }

        val args = mutableMapOf<KParameter, Any?>()

        for ((name, param) in constructorParams)
        {
            val prop = annotationProperties[name]
                ?: error("Parameter '$name' in metadata constructor not found in annotation ${this.annotationClass.simpleName}")

            val value = prop.get(this)

            // Optional: Convert arrays to sets for group and validator handling
            val expectedType = param.type.classifier

            val finalValue = when (value)
            {
                is Array<*> -> {

                    val mappedValues = value.map {

                        when (it) {
                            is Message -> {
                                MessageMetadata(text = it.text, language = it.lang)
                            }
                            is Annotation if ConstraintMetadata::class.java.isAssignableFrom(it.javaClass) -> it.convertToMetadata()
                            is Class<*> -> it.kotlin
                            else -> it
                        }
                    }

                    when (expectedType) {
                        Array::class -> mappedValues.toTypedArray()
                        Set::class -> mappedValues.toSet()
                        List::class -> mappedValues.toList()
                        Collection::class -> mappedValues.toCollection(ArrayList())
                        else -> mappedValues
                    }
                }

                // Recursively convert nested single annotation -> metadata
                is Annotation if expectedType is KClass<*> && ConstraintMetadata::class.java.isAssignableFrom(expectedType.java) -> {
                    value.convertToMetadata()
                }

                else -> {
                    if (!areTypesCompatible(prop.returnType.classifier, expectedType)) {
                        error("Type mismatch for property '$name': Annotation has ${prop.returnType.classifier}, but Metadata expects $expectedType")
                    }
                    value
                }
            }

            args[param] = finalValue
        }

        // Construct instance
        val instance = constructor.callBy(args)

        // If the ConstraintMetadata instance supports `placeHolders` or `appliesToContainer`,
        // set them dynamically via reflection (if they are mutable `var` properties).
        // - `placeHolders`: sets the placeholders for validation messages.
        // - `appliesToContainer`: indicates whether the constraint applies to container/aggregate types.
        // Any exceptions are caught and ignored to allow optional presence of these properties.
        runCatching {
            val appliesToContainerKey = ConstraintMetadata::appliesToContainer.name
            val appliesToContainerProp = metadataClass.members
                .filterIsInstance<kotlin.reflect.KMutableProperty1<Any, Any?>>()
                .firstOrNull { it.name == appliesToContainerKey }
            appliesToContainerProp?.set(instance, constraintAnn.appliesToContainer)
        }.getOrNull()

        return instance
    }


    /**
     * Determines whether two Kotlin types are compatible for assignment.
     *
     * Supports known conversions such as `Array<KClass<*>>` → `Set<KClass<*>>`,
     * and class assignability checks.
     */
    private fun areTypesCompatible(
        from: KClassifier?,
        to: KClassifier?
    ): Boolean
    {
        if (from == to) return true

        // Handle common special cases
        return when
        {
            isArrayOfKClass(from) && to == Set::class -> true
            from is KClass<*> && to is KClass<*> && from.java.isAssignableFrom(to.java) -> true
            else -> false
        }
    }


    /**
     * Checks whether the given classifier represents `Array<KClass<*>>`.
     */
    private fun isArrayOfKClass(type: KClassifier?) : Boolean
    {
        // Check raw class and generic type
        return type is KClass<*> &&
                type.java.isArray &&
                type.java.componentType == KClass::class.java
    }
}