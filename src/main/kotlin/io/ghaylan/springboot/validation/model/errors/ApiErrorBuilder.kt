package io.ghaylan.springboot.validation.model.errors

import io.ghaylan.springboot.validation.utils.PropNameAccessor
import java.lang.reflect.Field
import kotlin.reflect.KProperty

/**
 * Fluent builder for constructing [ApiError] instances.
 *
 * @property location Request section where the error originated.
 * @property code Error code representing the validation or business error type.
 */
class ApiErrorBuilder(
	private val location: ApiError.ErrorLocation,
	private val code: Enum<*>
) {
	
	private var field: String? = null
	private var message: String? = null
	private var data: Any? = null
	private var dataMap: HashMap<String, Any?>? = null
	
	
	/**
	 * Sets the field path for the error using a dot-notation string.
	 *
	 * @param field Dot-notation path to the invalid field
	 *              e.g., "user.name" or "items[0].price"
	 * @return This builder instance for chaining
	 */
	fun field(field: String): ApiErrorBuilder {
		this.field = field
		return this
	}
	
	/**
	 * Sets the field path for the error using a Kotlin property reference.
	 *
	 * @param property KProperty of the invalid field (e.g., User::name, Item::price)
	 * @return This builder instance for chaining
	 */
	fun field(property: KProperty<*>): ApiErrorBuilder {
		this.field = PropNameAccessor.getName(property)
		return this
	}
	
	/**
	 * Sets the field path for the error using a Java reflection field.
	 *
	 * @param field The Java [Field] representing the invalid property
	 * @return This builder instance for chaining
	 */
	fun field(field: Field): ApiErrorBuilder {
		this.field = PropNameAccessor.getName(field)
		return this
	}
	
	/**
	 * Sets a generic data object associated with this error.
	 *
	 * @param value Arbitrary data object to include in the error
	 * @return This builder instance for chaining
	 */
	fun data(value: Any?): ApiErrorBuilder {
		this.data = value
		return this
	}
	
	/**
	 * Adds a single key-value pair to the data map associated with this error.
	 *
	 * @param key The key identifying the piece of data
	 * @param value The value associated with the key
	 * @return This builder instance for chaining
	 */
	fun data(
		key: String,
		value: Any?
	): ApiErrorBuilder {
		if (dataMap == null) dataMap = HashMap()
		dataMap!![key] = value
		return this
	}
	
	/**
	 * Sets the message for this error.
	 *
	 * @param message The error message
	 * @return This builder instance for chaining
	 */
	fun message(message: String): ApiErrorBuilder {
		this.message = message
		return this
	}
	
	/**
	 * Builds and returns the configured [ApiError] instance.
	 * @return The constructed ApiError with all configured properties
	 */
	internal fun build(): ApiError = ApiError(
		path = field,
		code = code, message = message,
		location = location,
		data = dataMap?.ifEmpty { null }
			?: data)
}