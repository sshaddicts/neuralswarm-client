package com.github.sshaddicts.neuralclient.data

import com.fasterxml.jackson.annotation.JsonCreator
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

        @JsonIgnore
        val coder: Base64Coder
) {

    @JsonCreator
    constructor(token: String, image: String) : this(token, image, CommonBase64Coder())

    constructor(image: String) : this(anonToken, image)
    constructor(bytes: ByteArray) : this(anonToken, bytes)
    constructor(image: String, coder: Base64Coder) : this(anonToken, image, coder)
    constructor(bytes: ByteArray, coder: Base64Coder) : this(anonToken, bytes, coder)
    constructor(token: String, bytes: ByteArray) : this(token, bytes, CommonBase64Coder())
    constructor(token: String, bytes: ByteArray, coder: Base64Coder) : this(token, coder.encode(bytes), coder)

    @get:JsonIgnore
    val isAnonymous: Boolean = token == anonToken

    @get:JsonIgnore
    val bytes: ByteArray
        get() = coder.decode(image)

    val date = Date()

    companion object {
        private val anonToken = "anonymous"
    }
}