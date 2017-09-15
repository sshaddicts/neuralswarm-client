package com.github.sshaddicts.neuralclient.data

import com.fasterxml.jackson.annotation.JsonIgnore
import org.apache.commons.codec.binary.Base64
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
        val image: String,
        val details: ImageDetails
) {

    data class ImageDetails(
            val width: Int,
            val height: Int
    )

    val date = Date()

    @get:JsonIgnore
    val bytes: ByteArray
        get() = Base64.decodeBase64(image)

    companion object {
        fun create(bytes: ByteArray, details: ImageDetails, token: String) = ProcessImageRequest(
                token = token,
                image = Base64.encodeBase64String(bytes),
                details = details
        )
    }
}