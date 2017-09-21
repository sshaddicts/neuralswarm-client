package com.github.sshaddicts.neuralclient

import com.github.sshaddicts.neuralclient.data.History
import com.github.sshaddicts.neuralclient.data.Notification
import rx.Observable

/**
 * Successfully authenticated client.
 */
interface AuthenticatedClient : ProcessImageRequester {

    val token: String

    val notification: Observable<Notification>

    fun getFullHistory(): Observable<History>
}