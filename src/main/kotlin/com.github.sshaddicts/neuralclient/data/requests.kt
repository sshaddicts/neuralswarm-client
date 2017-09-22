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

data class ProcessImageRequest @JvmOverloads constructor(
        val token: String = anonToken,
        val image: String,

        @JsonIgnore
        val coder: Base64Coder = CommonBase64Coder()
) {

    @get:JsonIgnore
    val isAnonymous: Boolean = token == anonToken

    @get:JsonIgnore
    val bytes: ByteArray
        get() = coder.decode(image)

    val date = Date()

    companion object {
        private val anonToken = "anonymous"

        @JvmOverloads
        fun createFromBytes(token: String = anonToken, bytes: ByteArray, coder: Base64Coder = CommonBase64Coder()) =
                ProcessImageRequest(token, coder.encode(bytes), coder)
    }
}