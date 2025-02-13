package com.es.aplicacion.domain

data class DatosProvincia(
    val update_date: String,
    val size: Int,
    val data: List<Provincia>?,
    val warning: String?,
    val error: String?
)