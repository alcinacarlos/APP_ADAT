package com.es.aplicacion.service

import com.es.aplicacion.domain.DatosMunicipio
import com.es.aplicacion.domain.DatosProvincia
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class ExternalApiService(private val webClient: WebClient.Builder) {

    @Value("\${API_KEY}")
    private lateinit var apiKey: String

    fun obtenerProvinciasDesdeApi(): DatosProvincia? {
        return webClient.build()
            .get()
            .uri("https://apiv1.geoapi.es/provincias?type=JSON&key=$apiKey")
            .retrieve()
            .bodyToMono(DatosProvincia::class.java)
            .block() // ⚠️ Esto bloquea el hilo, usar `subscribe()` en código reactivo
    }

    fun obtenerMunicipiosDesdeApi(cpro:String): DatosMunicipio? {
        return webClient.build()
            .get()
            .uri("https://apiv1.geoapi.es/municipios?CPRO=${cpro}&type=JSON&key=$apiKey")
            .retrieve()
            .bodyToMono(DatosMunicipio::class.java)
            .block() // ⚠️ Esto bloquea el hilo, usar `subscribe()` en código reactivo
    }
}