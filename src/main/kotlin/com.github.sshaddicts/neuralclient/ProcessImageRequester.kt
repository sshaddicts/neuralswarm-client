package com.github.sshaddicts.neuralclient

import rx.Observable


interface ProcessImageRequester {
    fun processImage(bytes: ByteArray): Observable<Client.ProcessImageResponse>
}