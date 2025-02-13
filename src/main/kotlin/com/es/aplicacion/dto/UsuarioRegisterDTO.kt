package com.es.aplicacion.dto

data class UsuarioRegisterDTO(
    val username: String,
    val email: String,
    val password: String,
    val passwordRepeat: String,
    val calle: String,
    val numeroCalle: String,
    val cp: String,
    val municipio: String,
    val provincia: String,
    val rol: String?
)
