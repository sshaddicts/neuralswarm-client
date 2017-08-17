package com.github.sshaddicts.neuralclient

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import ws.wamp.jawampa.WampClient
import ws.wamp.jawampa.WampClientBuilder
import ws.wamp.jawampa.WampRouterBuilder
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider
import ws.wamp.jawampa.transport.netty.NettyWampConnectionConfig
import ws.wamp.jawampa.transport.netty.SimpleWampWebsocketListener
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals


object ClientSpec : Spek({
    describe("neuralswarm client") {
        it("should be able to request image processing from a server") {

            val done = CompletableFuture<Any>()

            val router = WampRouterBuilder().addRealm("realm1").build()

            val server = SimpleWampWebsocketListener(router, URI.create("ws://0.0.0.0:7779/api"), null)
            server.start()

            val client = Client("ws://localhost:7779/api", "realm1", "123")

            val wamp: WampClient = WampClientBuilder()
                    .withConnectorProvider(NettyWampClientConnectorProvider())
                    .withConnectionConfiguration(NettyWampConnectionConfig
                            .Builder()
                            .withMaxFramePayloadLength(10485760)
                            .build())
                    .withCloseOnErrors(false)
                    .withUri("ws://localhost:7779/api")
                    .withRealm("realm1")
                    .withInfiniteReconnects()
                    .withReconnectInterval(3, TimeUnit.SECONDS)
                    .build()

            wamp.statusChanged().subscribe {
                when (it) {
                    is WampClient.ConnectedState -> {

                        wamp.registerProcedure("process.image").subscribe { request ->
                            try {
                                val encoded: String = request.arguments().first().toString()
                                val bytes = Base64.getMimeDecoder().decode(encoded)

                                request.reply(ProcessedData(mapOf(
                                        "foo" to String(bytes)
                                )))
                            } catch (e: Throwable) {
                                println(e)
                            }
                        }

                        client.connected.subscribe {
                            Thread.sleep(100)
                            it.processImage("test".toByteArray()).subscribe {
                                assertEquals("test", it.items["foo"])
                                done.complete(null)
                            }
                        }

                        client.connect()
                    }
                }
            }

            wamp.open()

            done.get()

            server.stop()
        }
    }
})