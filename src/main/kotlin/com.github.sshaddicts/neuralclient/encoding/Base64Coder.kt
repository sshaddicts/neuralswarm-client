package com.github.sshaddicts.neuralclient.encoding


interface Base64Coder {
    fun encode(bytes: ByteArray): String
    fun decode(str: String): ByteArray
}