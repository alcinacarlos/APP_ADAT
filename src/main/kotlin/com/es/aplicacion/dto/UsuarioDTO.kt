package com.es.aplicacion.dto

import com.es.aplicacion.model.Direccion

data class UsuarioDTO(
    val username: String,
    val email: String,
    val direccion: Direccion,
    val rol: String?
)
