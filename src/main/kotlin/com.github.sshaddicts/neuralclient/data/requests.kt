package com.github.sshaddicts.neuralclient.data

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

data class HistoryRequest(
        val token: String
)

data class AuthenticationRequest(
        val username: String,
        val password: String
)

data class RegistrationRequest(
        val username: String,
        val password: String
)

data class ProcessImageRequest(
        val token: String,
        val image: String
) {

    val date = Date()

    @get:JsonIgnore
    val bytes: ByteArray
        get() = Base64.getMimeDecoder().decode(image)

    companion object {
        fun fromBytes(bytes: ByteArray, token: String) = ProcessImageRequest(
                token = token,
                image = Base64.getMimeEncoder().encodeToString(bytes)
        )
    }
}