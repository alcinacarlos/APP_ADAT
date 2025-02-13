package com.es.aplicacion.service

import com.es.aplicacion.dto.UsuarioDTO
import com.es.aplicacion.dto.UsuarioRegisterDTO
import com.es.aplicacion.error.exception.BadRequestException
import com.es.aplicacion.error.exception.NotFoundException
import com.es.aplicacion.error.exception.UnauthorizedException
import com.es.aplicacion.model.Direccion
import com.es.aplicacion.model.Usuario
import com.es.aplicacion.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UsuarioService : UserDetailsService {

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var apiService: ExternalApiService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder


    override fun loadUserByUsername(username: String?): UserDetails {
        val usuario: Usuario = usuarioRepository
            .findByUsername(username!!)
            .orElseThrow {
                UnauthorizedException("$username no existente")
            }

        return User.builder()
            .username(usuario.username)
            .password(usuario.password)
            .roles(usuario.roles)
            .build()
    }

    fun insertUser(usuarioInsertadoDTO: UsuarioRegisterDTO): UsuarioDTO? {

        if (usuarioInsertadoDTO.username.isBlank()
            || usuarioInsertadoDTO.password.isBlank()
            || usuarioInsertadoDTO.email.isBlank()
            || usuarioInsertadoDTO.passwordRepeat.isBlank()
            ) {
            throw BadRequestException("Uno o más campos vacios")
        }

        if (usuarioInsertadoDTO.password != usuarioInsertadoDTO.passwordRepeat){
            throw BadRequestException("Passwords do not match")
        }

        if (!usuarioRepository.findByUsername(usuarioInsertadoDTO.username).isEmpty){
            throw Exception("Usuario ${usuarioInsertadoDTO.username} ya está registrado")
        }

        if (usuarioInsertadoDTO.rol != null && usuarioInsertadoDTO.rol != "USER" && usuarioInsertadoDTO.rol != "ADMIN") {
            throw BadRequestException("Rol ${usuarioInsertadoDTO.rol} incorrecto")
        }

        //falta comprobar email

        // Realizo una llamada a una API externa para obtener todas las provincias de España
        val datosProvincias = apiService.obtenerProvinciasDesdeApi()
        var cpro = ""
        // Si los datos vienen rellenos entonces busco la provincia dentro del resultado de la llamada
        if (datosProvincias != null) {
            println(datosProvincias)
            if(datosProvincias.data != null) {
                val datos = datosProvincias.data.stream().filter {
                    it.PRO == usuarioInsertadoDTO.provincia.uppercase()
                }.findFirst().orElseThrow {
                    NotFoundException("Provincia ${usuarioInsertadoDTO.provincia.uppercase()} no válida")
                }
                cpro = datos.CPRO
            }
        }
        val datosMunicipio = apiService.obtenerMunicipiosDesdeApi(cpro)
        if (datosMunicipio != null) {
            if(datosMunicipio.data != null) {
                datosMunicipio.data.stream().filter {
                    it.DMUN50 == usuarioInsertadoDTO.municipio.uppercase()
                }.findFirst().orElseThrow {
                    NotFoundException("Municipio ${usuarioInsertadoDTO.municipio.uppercase()} no valido")
                }
            }
        }


        val user = Usuario(
            _id = null,
            username = usuarioInsertadoDTO.username,
            email = usuarioInsertadoDTO.email,
            password = passwordEncoder.encode(usuarioInsertadoDTO.password),
            roles = usuarioInsertadoDTO.rol ?: "USER",
            direccion = Direccion(
                usuarioInsertadoDTO.calle,
                usuarioInsertadoDTO.numeroCalle,
                usuarioInsertadoDTO.municipio,
                usuarioInsertadoDTO.provincia,
                usuarioInsertadoDTO.cp
            )
        )
        try {
            usuarioRepository.save(user)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }

        return UsuarioDTO(
            user.username,
            user.email,
            user.direccion,
            user.roles
        )

    }
}