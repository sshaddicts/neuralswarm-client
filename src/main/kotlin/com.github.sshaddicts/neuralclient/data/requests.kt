package com.github.sshaddicts.neuralclient.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.sshaddicts.neuralclient.encoding.Base64Coder
import com.github.sshaddicts.neuralclient.encoding.CommonBase64Coder
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
        val details: ImageDetails,
        @JsonIgnore
        private val coder: Base64Coder = CommonBase64Coder()
) {

    data class ImageDetails(
            val width: Int,
            val height: Int
    )

    val date = Date()

    @get:JsonIgnore
    val bytes: ByteArray
        get() = coder.decode(image)

    companion object {
        fun create(bytes: ByteArray, details: ImageDetails, token: String, coder: Base64Coder) = ProcessImageRequest(
                token = token,
                image = coder.encode(bytes),
                details = details
        )
    }
}