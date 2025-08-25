package io.ghaylan.springboot.validation.constraints.validators.string.password

import io.ghaylan.springboot.validation.constraints.ConstraintMetadata
import io.ghaylan.springboot.validation.constraints.message.MessageMetadata
import io.ghaylan.springboot.validation.constraints.annotations.Password.PasswordStrength
import kotlin.reflect.KClass

data class PasswordConstraint(
    val minLength : Int,
    val maxLength : Int,
    val requireUppercase : Boolean,
    val requireLowercase : Boolean,
    val requireDigit : Boolean,
    val requireSpecialChar: Boolean,
    val allowedSpecialChars : String,
    val minEntropy : PasswordStrength,
    val noSequentialChars : Boolean,
    val noRepetitivePatterns : Boolean,
    override val groups: Set<KClass<*>>,
    override val messages : Set<MessageMetadata>
) : ConstraintMetadata()