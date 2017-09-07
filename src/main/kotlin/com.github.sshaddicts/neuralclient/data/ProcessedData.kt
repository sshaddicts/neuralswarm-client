package com.github.sshaddicts.neuralclient.data

import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.*


data class ProcessedData (
        val items: List<ObjectNode>,
        val processDate: Date = Date()
)