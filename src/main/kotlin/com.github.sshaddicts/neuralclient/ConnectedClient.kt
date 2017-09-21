package com.github.sshaddicts.neuralclient

import rx.Observable

/**
 * Connected, not authenticated client.
 */
interface ConnectedClient : ProcessImageRequester {
    fun auth(username: String, password: String): Observable<AuthenticatedClient>
    fun register(username: String, password: String): Observable<AuthenticatedClient>
}