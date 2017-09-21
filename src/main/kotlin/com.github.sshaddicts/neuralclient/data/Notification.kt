package com.github.sshaddicts.neuralclient.data

/**
 * Message from server.
 */
data class Notification(
        val type: String,
        val payload: Map<String, String>
)