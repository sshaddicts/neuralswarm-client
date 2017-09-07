package com.github.sshaddicts.neuralclient

import com.github.sshaddicts.neuralclient.data.History
import com.github.sshaddicts.neuralclient.data.ProcessedData
import rx.Observable


interface AuthenticatedClient {

    val token: String

    fun processImage(bytes: ByteArray): Observable<ProcessedData>
    fun getFullHistory(): Observable<History>
}