package com.kaylakautai.inkle.auth

class FakeGoogleIdTokenProvider : GoogleIdTokenProvider {
    /** What [requestIdToken] does: return a token, throw, or suspend on a deferred. */
    var onRequest: suspend () -> String = { "fake-id-token" }

    var requestCount = 0
        private set

    override suspend fun requestIdToken(): String {
        requestCount++
        return onRequest()
    }
}
