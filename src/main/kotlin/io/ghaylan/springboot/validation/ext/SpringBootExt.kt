package io.ghaylan.springboot.validation.ext

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.collections.firstOrNull
import kotlin.jvm.java
import kotlin.text.isNotBlank


/**
 * Retrieves the name of the [RequestParam] annotation for this [Parameter], falling back to the parameter name if necessary.
 *
 * The function checks the following in order:
 * 1. The `name` property of the [RequestParam] annotation.
 * 2. The `value` property of the [RequestParam] annotation.
 * 3. The parameter's name.
 *
 * @return The first non-blank name found from the [RequestParam] annotation or the parameter itself.
 */
internal fun Parameter.requestParamName() : String
{
	val annotation = getAnnotation(RequestParam::class.java)
	
	return listOfNotNull(
        annotation?.name,
        annotation?.value,
        this.name
    ).firstOrNull(String::isNotBlank) ?: this.name
}


/**
 * Retrieves the name of the [RequestHeader] annotation for this [Parameter], falling back to the parameter name if necessary.
 *
 * The function checks the following in order:
 * 1. The `name` property of the [RequestHeader] annotation.
 * 2. The `value` property of the [RequestHeader] annotation.
 * 3. The parameter's name.
 *
 * @return The first non-blank name found from the [RequestHeader] annotation or the parameter itself.
 */
internal fun Parameter.requestHeaderName() : String
{
	val annotation = getAnnotation(RequestHeader::class.java)

	return listOfNotNull(
        annotation?.name,
        annotation?.value,
        this.name
    ).firstOrNull(String::isNotBlank) ?: this.name
}


/**
 * Retrieves the name of the [PathVariable] annotation for this [Parameter], falling back to the parameter name if necessary.
 *
 * The function checks the following in order:
 * 1. The `name` property of the [PathVariable] annotation.
 * 2. The `value` property of the [PathVariable] annotation.
 * 3. The parameter's name.
 *
 * @return The first non-blank name found from the [PathVariable] annotation or the parameter itself.
 */
internal fun Parameter.pathVariableName() : String
{
	val annotation = getAnnotation(PathVariable::class.java)

	return listOfNotNull(
        annotation?.name,
        annotation?.value,
        this.name
    ).firstOrNull(String::isNotBlank) ?: this.name
}


internal fun Method.generateRequestId() : String = "${declaringClass.name}#${name}#${parameterCount}"