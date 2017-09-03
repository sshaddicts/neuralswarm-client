package com.github.sshaddicts.neuralclient

import com.github.sshaddicts.neuralclient.data.ProcessedData
import java.util.concurrent.CompletableFuture


fun main(args: Array<String>) {

    val client = Client("ws://localhost:7778/api", "realm1")

    val done = CompletableFuture<ProcessedData>()

    client.connected.subscribe({
        it.register("test", "test").subscribe({
            it.processImage("test".toByteArray()).subscribe({

                done.complete(it)

            }, { it.printStackTrace() })
        }, { it.printStackTrace() })
    }, { it.printStackTrace() })

    client.connect()

    done.get().items.forEach {
        println("${it.first} - ${it.second}")
    }
}