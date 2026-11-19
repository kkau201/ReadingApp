package com.kaylakautai.inkle

import android.app.Activity
import android.app.Application
import androidx.fragment.app.FragmentActivity
import com.kaylakautai.inkle.common.ActivityProvider
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class InkleApp: Application(), ActivityProvider {
    private val activityLifecycleCallbacks = ReadingActivityLifecycleCallbacks()

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    override fun currentActivity(): Activity? {
        return activityLifecycleCallbacks.currentActivity
    }

    override fun currentFragmentActivity(): FragmentActivity? {
        return currentActivity() as? FragmentActivity
    }
}