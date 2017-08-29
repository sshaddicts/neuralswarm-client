package com.github.sshaddicts.neuralclient

import rx.Observable


interface ConnectedClient {
    fun auth(username: String, password: String): Observable<AuthenticatedClient>
    fun register(username: String, password: String): Observable<AuthenticatedClient>
}