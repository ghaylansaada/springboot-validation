package io.ghaylan.springboot.validation.constraints

data class ErrorMessageMetadata(
    val language : String,
    val text: String,
    val errorCode : String)
