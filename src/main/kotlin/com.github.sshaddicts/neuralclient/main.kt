package com.github.sshaddicts.neuralclient

import java.io.File

fun main(args: Array<String>) {
    val client = Client("ws://localhost:7778/api", "realm1", "123")

    client.connected.subscribe {
        it.processImage(File("image.jpg").readBytes()).subscribe {
            println(it.items["size"])
        }
    }

    readLine()
}