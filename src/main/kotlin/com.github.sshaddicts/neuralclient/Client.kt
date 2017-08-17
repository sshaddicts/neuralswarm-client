package com.github.sshaddicts.neuralclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.treeToValue
import rx.Observable
import ws.wamp.jawampa.WampClient
import ws.wamp.jawampa.WampClientBuilder
import ws.wamp.jawampa.connection.IWampClientConnectionConfig
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider
import ws.wamp.jawampa.transport.netty.NettyWampConnectionConfig
import java.util.*

import java.util.concurrent.TimeUnit


class Client(
        uri: String,
        realm: String,
        private val token: String
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

    val connected: Observable<ConnectedClient>  = rx.Observable.create { subscriber ->
        wamp.statusChanged().subscribe {
            when(it) {
                is WampClient.ConnectedState -> {
                    subscriber.onNext(object: ConnectedClient {
                        override fun processImage(bytes: ByteArray): Observable<ProcessedData> {

                            val encoded = Base64.getMimeEncoder().encodeToString(bytes)

                            return Observable.create { subscriber ->
                                wamp.call("process.image", encoded, token).subscribe {
                                    val data: ProcessedData = mapper.treeToValue(it.arguments().first())

                                    subscriber.onNext(data)
                                    subscriber.onCompleted()
                                }
                            }
                        }
                    })

                    subscriber.onCompleted()
                }
            }
        }
    }

    fun connect() = wamp.open()
}