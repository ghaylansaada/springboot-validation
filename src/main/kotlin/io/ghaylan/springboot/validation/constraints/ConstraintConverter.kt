package io.ghaylan.springboot.validation.constraints

import kotlin.reflect.*
import kotlin.reflect.jvm.jvmName

/**
 * Converts constraint annotations into [ConstraintMetadata] instances by matching annotation
 * properties to metadata constructor parameters. Handles type coercion (e.g., `Array<KClass<*>>`
 * to `Set<KClass<*>>`), nested annotations, and runtime type validation.
 */
object ConstraintConverter {

    /** Converts this annotation to its corresponding [ConstraintMetadata] via reflective constructor mapping. */
    fun Annotation.convertToMetadata(): ConstraintMetadata {
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
	    
	    for ((name, param) in constructorParams) {
            val prop = annotationProperties[name]
                ?: error("Parameter '$name' in metadata constructor not found in annotation ${this.annotationClass.simpleName}")

            val value = prop.get(this)

            // Optional: Convert arrays to sets for group and validator handling
            val expectedType = param.type.classifier
		    val finalValue = when (value) {
                is Array<*> -> {

                    val mappedValues = value.map {

                        when (it) {
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
                .filterIsInstance<KMutableProperty1<Any, Any?>>()
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
    ): Boolean {
        if (from == to) return true

        // Handle common special cases
	    return when {
            isArrayOfKClass(from) && to == Set::class -> true
            from is KClass<*> && to is KClass<*> && from.java.isAssignableFrom(to.java) -> true
            else -> false
        }
    }
	
	/**
     * Checks whether the given classifier represents `Array<KClass<*>>`.
     */
	private fun isArrayOfKClass(type: KClassifier?): Boolean {
        // Check raw class and generic type
        return type is KClass<*> &&
                type.java.isArray &&
                type.java.componentType == KClass::class.java
    }
}