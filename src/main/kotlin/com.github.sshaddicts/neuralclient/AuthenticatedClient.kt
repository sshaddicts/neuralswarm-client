package com.github.sshaddicts.neuralclient

import com.github.sshaddicts.neuralclient.data.History
import com.github.sshaddicts.neuralclient.data.ProcessImageRequest
import com.github.sshaddicts.neuralclient.data.ProcessedData
import rx.Observable


interface AuthenticatedClient {

    val token: String

    fun processImage(bytes: ByteArray, width: Int, height: Int): Observable<ProcessedData>
    fun processImage(bytes: ByteArray, details: ProcessImageRequest.ImageDetails): Observable<ProcessedData>
    fun getFullHistory(): Observable<History>
}