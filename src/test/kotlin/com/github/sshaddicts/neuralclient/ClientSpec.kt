package com.github.sshaddicts.neuralclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.github.sshaddicts.neuralclient.data.AuthenticationRequest
import com.github.sshaddicts.neuralclient.data.ProcessImageRequest
import com.github.sshaddicts.neuralclient.data.ProcessedData
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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

object ClientSpec : Spek({

    val mapper = ObjectMapper().registerKotlinModule()

    describe("neuralswarm client") {

        it("should be able to request image processing from a server") {

            val done: CompletableFuture<Any> = CompletableFuture()

            val router = WampRouterBuilder().addRealm("realm1").build()

            val server = SimpleWampWebsocketListener(router, URI.create("ws://0.0.0.0:7779/api"), null)
            server.start()

            val client = Client("ws://localhost:7779/api", "realm1")

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

            wamp.statusChanged().filter { it is WampClient.ConnectedState }.subscribe {

                wamp.registerProcedure("user.auth").subscribe { request ->
                    try {
                        val authRequest = mapper.treeToValue<AuthenticationRequest>(request.keywordArguments())

                        assertEquals("foo", authRequest.username)
                        assertEquals("bar", authRequest.password)

                        request.reply("txTFgqD8N9+fI37T4pw6nS8m7DYGswaR6OT16bbjWg9AjEMT6r/bB4XHv1/L3gqTUsl/AgvBjM23l1HTgzKmTExeqG0zNp9dZ+TNoDiTUKMk9eQXQ6nkMkZBKYok/4uB")

                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }

                wamp.registerProcedure("process.image").subscribe { request ->
                    try {
                        val processRequest = mapper.treeToValue<ProcessImageRequest>(request.keywordArguments())

                        assertEquals(20.toDouble(), processRequest.details.width)
                        assertEquals(20.toDouble(), processRequest.details.height)

                        val node = mapper.createObjectNode()

                        node.put("name", String(processRequest.bytes))
                        node.put("price", 123.toDouble())

                        request.reply(mapper.createArrayNode(), mapper.valueToTree(ProcessedData(
                                listOf(node)
                        )))
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }

                client.connected.flatMap { it.auth("foo", "bar") }
                        .subscribe({
                            Thread.sleep(100)

                            assertEquals("txTFgqD8N9+fI37T4pw6nS8m7DYGswaR6OT16bbjWg9AjEMT6r/bB4XHv1/L3gqTUsl/AgvBjM23l1HTgzKmTExeqG0zNp9dZ+TNoDiTUKMk9eQXQ6nkMkZBKYok/4uB", it.token)

                            it.processImage("test".toByteArray(), 20.toDouble(), 20.toDouble()).subscribe {
                                assertEquals("test", it.items.first()["name"].textValue())
                                done.complete(true)
                            }
                        }, ::println)

                client.connect()
            }

            wamp.open()

            done.get(25, TimeUnit.SECONDS)

            server.stop()
        }
    }
})