package com.github.sshaddicts.neuralclient

import com.github.sshaddicts.neuralclient.data.ProcessImageResponse
import rx.Observable


interface ProcessImageRequester {
    fun processImage(bytes: ByteArray): Observable<ProcessImageResponse>
}