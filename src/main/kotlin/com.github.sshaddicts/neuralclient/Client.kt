package com.github.sshaddicts.neuralclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.github.sshaddicts.neuralclient.data.*
import rx.Observable
import ws.wamp.jawampa.WampClient
import ws.wamp.jawampa.WampClientBuilder
import ws.wamp.jawampa.connection.IWampClientConnectionConfig
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider
import ws.wamp.jawampa.transport.netty.NettyWampConnectionConfig
import java.util.concurrent.TimeUnit

class Client(
        uri: String,
        realm: String
) {
    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    private val config: IWampClientConnectionConfig = NettyWampConnectionConfig
            .Builder()
            .withMaxFramePayloadLength(10485760)
            .build()

    private val wamp: WampClient = WampClientBuilder()
            .withConnectorProvider(NettyWampClientConnectorProvider())
            .withConnectionConfiguration(config)
            .withCloseOnErrors(false)
            .withUri(uri)
            .withRealm(realm)
            .withInfiniteReconnects()
            .withReconnectInterval(3, TimeUnit.SECONDS)
            .build()

    val connected: Observable<ConnectedClient> = wamp.statusChanged()
            .filter { it is WampClient.ConnectedState }
            .map { createConnectedClient() }

    fun connect() = wamp.open()

    private fun createConnectedClient() = object : ConnectedClient {

        override fun auth(username: String, password: String): Observable<AuthenticatedClient> =
                call("user.auth", AuthenticationRequest(username, password)).map {
                    createAuthenticatedClient(it.arguments().first().textValue())
                }

        override fun register(username: String, password: String): Observable<AuthenticatedClient> =
                call("user.register", RegistrationRequest(username, password)).map {
                    createAuthenticatedClient(it.arguments().first().textValue())
                }
    }

    private fun createAuthenticatedClient(token: String) = object : AuthenticatedClient {
        override fun processImage(bytes: ByteArray, width: Double, height: Double): Observable<ProcessedData> =
                processImage(bytes, ProcessImageRequest.ImageDetails(width, height))

        override fun getFullHistory(): Observable<History> =
                call("history.get", HistoryRequest(token)).map {
                    mapper.treeToValue<History>(it.keywordArguments())
                }

        override fun processImage(bytes: ByteArray, details: ProcessImageRequest.ImageDetails): Observable<ProcessedData> =
                call("process.image", ProcessImageRequest.create(bytes, details, token)).map {
                    mapper.treeToValue<ProcessedData>(it.keywordArguments())
                }

        override val token: String = token
    }

    private fun call(topic: String, item: Any) =
            wamp.call(topic, mapper.createArrayNode(), mapper.valueToTree(item))
}