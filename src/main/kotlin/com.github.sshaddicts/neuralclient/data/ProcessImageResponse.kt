package com.github.sshaddicts.neuralclient.data

import java.util.*


data class ProcessImageResponse(
        val data: ProcessedData,
        val overlay: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProcessImageResponse

        if (data != other.data) return false
        if (!Arrays.equals(overlay, other.overlay)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + Arrays.hashCode(overlay)
        return result
    }
}