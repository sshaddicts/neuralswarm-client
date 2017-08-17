package com.github.sshaddicts.neuralclient

import rx.Observable


interface ConnectedClient {
    fun processImage(bytes: ByteArray): Observable<ProcessedData>
}