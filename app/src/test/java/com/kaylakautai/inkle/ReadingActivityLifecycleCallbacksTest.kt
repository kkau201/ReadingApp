package com.kaylakautai.inkle

import android.app.Activity
import io.mockk.mockk
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class ReadingActivityLifecycleCallbacksTest {

    private val callbacks = ReadingActivityLifecycleCallbacks()
    private val first = mockk<Activity>(relaxed = true)
    private val second = mockk<Activity>(relaxed = true)

    @Test
    fun tracksMostRecentlyResumedActivity() {
        callbacks.onActivityCreated(first, null)
        callbacks.onActivityResumed(first)
        assertSame(first, callbacks.currentActivity)

        callbacks.onActivityCreated(second, null)
        callbacks.onActivityResumed(second)
        assertSame(second, callbacks.currentActivity)
    }

    @Test
    fun clearsActivityWhenItIsDestroyed() {
        callbacks.onActivityResumed(first)

        callbacks.onActivityDestroyed(first)

        assertNull(callbacks.currentActivity)
    }

    @Test
    fun keepsCurrentActivityWhenADifferentOneIsDestroyed() {
        // e.g. on rotation the new instance is created before the old one is destroyed
        callbacks.onActivityResumed(first)
        callbacks.onActivityCreated(second, null)

        callbacks.onActivityDestroyed(first)

        assertSame(second, callbacks.currentActivity)
    }
}
