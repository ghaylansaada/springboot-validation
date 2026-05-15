package io.ghaylan.springboot.validation.ext

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Parameter

/**
 * Returns the effective query-parameter name: `@RequestParam.name`, then `.value`, then the parameter's declared name.
 */
internal fun Parameter.requestParamName(): String {
	val annotation = getAnnotation(RequestParam::class.java)
	
	return annotation?.name?.ifBlank { null }
		?: annotation?.value?.ifBlank { null }
		?: this.name
}

/**
 * Returns the effective header name: `@RequestHeader.name`, then `.value`, then the parameter's declared name.
 */
internal fun Parameter.requestHeaderName(): String {
	val annotation = getAnnotation(RequestHeader::class.java)
	
	return annotation?.name?.ifBlank { null }
		?: annotation?.value?.ifBlank { null }
		?: this.name
}

/**
 * Returns the effective path-variable name: `@PathVariable.name`, then `.value`, then the parameter's declared name.
 */
internal fun Parameter.pathVariableName(): String {
	val annotation = getAnnotation(PathVariable::class.java)
	
	return annotation?.name?.ifBlank { null }
		?: annotation?.value?.ifBlank { null }
		?: this.name
}

/** Returns the [JsonProperty] value if present on this field, otherwise the field's declared name. */
internal fun Field.bodyFieldName(): String {
	val annotation = getAnnotation(JsonProperty::class.java)
	
	return annotation?.value?.ifBlank { null }
		?: this.name
}

/**
 * Returns a unique method identifier in the form `com.example.MyController#getUser(String,int)`.
 * Distinguishes overloads by including parameter types.
 */
internal fun Method.getUniqueIdentifier(): String {
	val clazz = this.declaringClass.name
	val method = this.name
	val params = this.parameterTypes.joinToString(",") { it.simpleName }
	return "$clazz#$method($params)"
}