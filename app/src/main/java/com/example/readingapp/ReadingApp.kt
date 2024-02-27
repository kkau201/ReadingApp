package com.example.readingapp

import android.app.Activity
import android.app.Application
import androidx.fragment.app.FragmentActivity
import com.example.readingapp.common.ActivityProvider
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ReadingApp: Application(), ActivityProvider {
    private val activityLifecycleCallbacks = ReadingActivityLifecycleCallbacks()

    override fun currentActivity(): Activity? {
        return activityLifecycleCallbacks.currentActivity
    }

    override fun currentFragmentActivity(): FragmentActivity? {
        return currentActivity() as? FragmentActivity
    }
}