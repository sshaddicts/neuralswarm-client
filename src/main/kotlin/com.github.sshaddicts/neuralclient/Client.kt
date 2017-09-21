package com.github.sshaddicts.neuralclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.github.sshaddicts.neuralclient.data.*
import com.github.sshaddicts.neuralclient.encoding.Base64Coder
import com.github.sshaddicts.neuralclient.encoding.CommonBase64Coder
import rx.Observable
import ws.wamp.jawampa.Reply
import ws.wamp.jawampa.WampClient
import ws.wamp.jawampa.WampClient.State
import ws.wamp.jawampa.WampClientBuilder
import ws.wamp.jawampa.connection.IWampClientConnectionConfig
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider
import ws.wamp.jawampa.transport.netty.NettyWampConnectionConfig
import java.util.concurrent.TimeUnit

/**
 * Main package's class that uses [WampClient] to communicate with realm.
 */
class Client(
        uri: String,
        realm: String,
        maxFramePayloadLength: Int,
        private val coder: Base64Coder
) {
    private val `one megabyte` = 10240

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    private val config: IWampClientConnectionConfig = NettyWampConnectionConfig
            .Builder()
            .withMaxFramePayloadLength(maxFramePayloadLength * `one megabyte`)
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

    val status: Observable<State>
        get() = wamp.statusChanged()

    constructor(uri: String, realm: String, maxFramePayloadLength: Int) : this(uri, realm, maxFramePayloadLength, CommonBase64Coder())
    constructor(uri: String, realm: String) : this(uri, realm, 20)

    fun connect() = wamp.open()
    fun disconnect() = wamp.close()

    private fun createConnectedClient() = object : ConnectedClient {
        override fun processImage(bytes: ByteArray): Observable<ProcessImageResponse> =
                call("process.image", ProcessImageRequest(bytes, coder)).map {
                    mapper `will handle` it
                }

        override fun auth(username: String, password: String): Observable<AuthenticatedClient> =
                call("user.auth", AuthenticationRequest(username, password)).map {
                    createAuthenticatedClient(it.arguments().first().textValue(), username)
                }

        override fun register(username: String, password: String): Observable<AuthenticatedClient> =
                call("user.register", RegistrationRequest(username, password)).map {
                    createAuthenticatedClient(it.arguments().first().textValue(), username)
                }
    }

    private fun createAuthenticatedClient(token: String, username: String) = object : AuthenticatedClient {
        override val notification: Observable<Notification> = Observable.merge(
                wamp.makeSubscription("broadcast")
                        .map { mapper.treeToValue<Notification>(it.keywordArguments()) },
                wamp.makeSubscription(coder.encode(username.toByteArray()))
                        .map { mapper.treeToValue<Notification>(it.keywordArguments()) }
        )

        override fun getFullHistory(): Observable<History> =
                call("history.get", HistoryRequest(token)).map {
                    mapper.treeToValue<History>(it.keywordArguments())
                }

        override fun processImage(bytes: ByteArray): Observable<ProcessImageResponse> =
                call("process.image", ProcessImageRequest(token, bytes, coder)).map {
                    mapper `will handle` it
                }

        override val token: String = token
    }

    private infix fun ObjectMapper.`will handle`(response: Reply) = ProcessImageResponse(
            this.treeToValue(response.keywordArguments()),
            coder.decode(response.arguments().first().textValue())
    )

    private fun call(topic: String, item: Any) =
            wamp.call(topic, mapper.createArrayNode(), mapper.valueToTree(item))
}