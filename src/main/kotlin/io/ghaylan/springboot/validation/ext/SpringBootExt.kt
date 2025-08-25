package io.ghaylan.springboot.validation.ext

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.jvm.java

/**
 * Retrieves the name of the [RequestParam] annotation for this [Parameter],
 * falling back to the parameter name if necessary.
 *
 * The function checks the following in order:
 * 1. The `name` property of the [RequestParam] annotation.
 * 2. The `value` property of the [RequestParam] annotation.
 * 3. The parameter's declared name.
 *
 * @receiver The [Parameter] from which to extract the request parameter name.
 * @return The first non-blank name found from the [RequestParam] annotation or the parameter itself.
 */
internal fun Parameter.requestParamName() : String
{
	val annotation = getAnnotation(RequestParam::class.java)

    return annotation?.name?.ifBlank { null }
        ?: annotation?.value?.ifBlank { null }
        ?: this.name
}


/**
 * Retrieves the name of the [RequestHeader] annotation for this [Parameter],
 * falling back to the parameter name if necessary.
 *
 * The function checks the following in order:
 * 1. The `name` property of the [RequestHeader] annotation.
 * 2. The `value` property of the [RequestHeader] annotation.
 * 3. The parameter's declared name.
 *
 * @receiver The [Parameter] from which to extract the request header name.
 * @return The first non-blank name found from the [RequestHeader] annotation or the parameter itself.
 */
internal fun Parameter.requestHeaderName() : String
{
	val annotation = getAnnotation(RequestHeader::class.java)

    return annotation?.name?.ifBlank { null }
        ?: annotation?.value?.ifBlank { null }
        ?: this.name
}


/**
 * Retrieves the name of the [PathVariable] annotation for this [Parameter],
 * falling back to the parameter name if necessary.
 *
 * The function checks the following in order:
 * 1. The `name` property of the [PathVariable] annotation.
 * 2. The `value` property of the [PathVariable] annotation.
 * 3. The parameter's declared name.
 *
 * @receiver The [Parameter] from which to extract the path variable name.
 * @return The first non-blank name found from the [PathVariable] annotation or the parameter itself.
 */
internal fun Parameter.pathVariableName() : String
{
	val annotation = getAnnotation(PathVariable::class.java)

    return annotation?.name?.ifBlank { null }
        ?: annotation?.value?.ifBlank { null }
        ?: this.name
}


/**
 * Retrieves the name of a request body field represented by this [Field],
 * considering the [JsonProperty] annotation if present.
 *
 * The function checks the following in order:
 * 1. The `value` property of the [JsonProperty] annotation.
 * 2. The field's declared name.
 *
 * @receiver The [Field] from which to extract the request body field name.
 * @return The first non-blank name found from the [JsonProperty] annotation or the field itself.
 */
internal fun Field.bodyFieldName() : String
{
    val annotation = getAnnotation(JsonProperty::class.java)

    return annotation?.value?.ifBlank { null } ?: this.name
}


/**
 * Generates a unique identifier for this [Method],
 * composed of the fully qualified class name, method name, and parameter count.
 *
 * The format is: `ClassName#methodName#parameterCount`.
 *
 * @receiver The [Method] for which to generate the identifier.
 * @return A unique string identifier for the method.
 */
internal fun Method.generateRequestId() : String = "${declaringClass.name}#${name}#${parameterCount}"