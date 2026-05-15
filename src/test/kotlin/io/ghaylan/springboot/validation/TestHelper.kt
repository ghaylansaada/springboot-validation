package io.ghaylan.springboot.validation

import io.ghaylan.springboot.validation.accessor.AccessorRegistry
import io.ghaylan.springboot.validation.groups.OnDefault
import io.ghaylan.springboot.validation.model.ValidationContext
import io.ghaylan.springboot.validation.model.ValidationContextValue
import io.ghaylan.springboot.validation.model.errors.ApiError.ErrorLocation
import io.ghaylan.springboot.validation.schema.RequestInputSchema.PropertySpec
import io.ghaylan.springboot.validation.utils.ReflectionUtils
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeInfo
import io.ghaylan.springboot.validation.utils.ReflectionUtils.TypeKind

object TestHelper {

    fun defaultContext(
        fieldPath: String = "testField",
        fieldName: String = "testField",
        location: ErrorLocation = ErrorLocation.BODY,
        stopOnFirstError: Boolean = true,
        groups: Set<kotlin.reflect.KClass<*>> = setOf(OnDefault::class),
        containerObject: ValidationContextValue<Any>? = null,
        array: ValidationContextValue<List<Any>>? = null,
        type: TypeInfo? = null
    ): ValidationContext {
        return ValidationContext(
            fieldPath = fieldPath,
            fieldName = fieldName,
            type = type,
            location = location,
            stopOnFirstError = stopOnFirstError,
            groups = groups,
            containerObject = containerObject,
            array = array
        )
    }

    fun <T : Any> contextWithSiblingProperty(
        containerInstance: T,
        containerClass: Class<T>,
        fieldPath: String = "testField",
        fieldName: String = "testField"
    ): ValidationContext {
        val fields = containerClass.declaredFields
        val schema = fields.associate { field ->
            val resolvedName = field.name
            resolvedName to PropertySpec(
                realName = field.name,
                resolvedName = resolvedName,
                typeInfo = ReflectionUtils.infoFromField(field),
                accessor = AccessorRegistry.getOrCreate(containerClass, field.name, resolvedName),
                nested = emptyMap(),
                constraints = emptyMap()
            )
        }
        val containerValue = ValidationContextValue<Any>(
            value = containerInstance,
            schema = schema,
            type = ReflectionUtils.infoFromClass(containerClass)
        )
        return defaultContext(
            fieldPath = fieldPath,
            fieldName = fieldName,
            containerObject = containerValue
        )
    }

    fun arrayContext(
        elements: List<Any>,
        fieldPath: String = "testField",
        fieldName: String = "testField",
        type: TypeInfo? = null
    ): ValidationContext {
        val arrayValue = ValidationContextValue(
            value = elements,
            schema = emptyMap<String, PropertySpec>(),
            type = type ?: TypeInfo(Collection::class, List::class, TypeKind.STRING_ARRAY)
        )
        return defaultContext(
            fieldPath = fieldPath,
            fieldName = fieldName,
            array = arrayValue,
            type = type
        )
    }
}
