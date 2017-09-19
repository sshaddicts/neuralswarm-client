package com.github.sshaddicts.neuralclient

import com.github.sshaddicts.neuralclient.data.History
import rx.Observable


interface AuthenticatedClient : ProcessImageRequester {

    val token: String

    fun getFullHistory(): Observable<History>
}