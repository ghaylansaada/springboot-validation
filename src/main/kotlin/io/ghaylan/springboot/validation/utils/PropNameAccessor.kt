package io.ghaylan.springboot.validation.utils

import com.fasterxml.jackson.annotation.JsonProperty
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField

/**
 * Utility for resolving the "effective" property name of a class member.
 *
 * This accessor provides a unified way to obtain the name of a Java [Field]
 * or Kotlin [KProperty], taking into account Jackson's [JsonProperty] annotation
 * when present. If the annotation is not found, the natural field or property name
 * is used as a fallback.
 *
 * To improve performance, results are cached in a [ConcurrentHashMap].
 * The cache key is a combination of the declaring class and the member name,
 * ensuring that repeated lookups are efficient and safe across threads.
 *
 * ## Features
 * - Supports both Java reflection ([Field]) and Kotlin reflection ([KProperty]).
 * - Respects the [JsonProperty] annotation, returning its configured value.
 * - Falls back to the declared name when no annotation is present.
 * - Gracefully handles Kotlin properties without a backing Java field
 *   (e.g., computed or delegated properties) without throwing an error.
 */
object PropNameAccessor
{
    /**
     * Unique cache key combining the declaring class and the raw member name.
     *
     * This ensures uniqueness across classes even if fields/properties
     * share the same name in different classes.
     */
    private data class Key(val clazz: Class<*>, val memberName: String)

    /** Thread-safe cache of resolved property names. */
    private val cache = ConcurrentHashMap<Key, String>()


    /**
     * Resolves the effective property name for the given Java [Field].
     *
     * @param field the Java reflection [Field] to inspect
     * @return the name to be used:
     *  - If [JsonProperty] is present on the field, its `value`
     *  - Otherwise, the raw field name
     */
    fun getName(field: Field): String
    {
        val key = Key(field.declaringClass, field.name)
        return cache.computeIfAbsent(key) {
            field.getAnnotation(JsonProperty::class.java)?.value ?: field.name
        }
    }


    /**
     * Resolves the effective property name for the given Kotlin [KProperty].
     *
     * @param property the Kotlin reflection [KProperty] to inspect
     * @return the name to be used:
     *  - If [JsonProperty] is present on the property, its `value`
     *  - Otherwise, the raw property name
     *
     * If the property does not have a backing Java field (e.g., is computed or delegated),
     * the declaring class is derived from the property's [KProperty] reference itself,
     * and the property name is still returned.
     */
    fun getName(property: KProperty<*>): String
    {
        val declaringClass = property.javaField?.declaringClass ?: property::class.java
        val key = Key(declaringClass, property.name)
        return cache.computeIfAbsent(key) {
            property.findAnnotation<JsonProperty>()?.value ?: property.name
        }
    }
}