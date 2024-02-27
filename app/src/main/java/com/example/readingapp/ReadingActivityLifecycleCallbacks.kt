package com.example.readingapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

class ReadingActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    var currentActivity: Activity? = null

    override fun onActivityPaused(activity: Activity) {
        Log.d("ReadingAppLifecycle", "onActivityPaused: " + activity.javaClass.name)
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d("ReadingAppLifecycle", "onActivityResumed: " + activity.javaClass.name)
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        Log.d("ReadingAppLifecycle", "onActivityStarted: " + activity.javaClass.name)
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d("ReadingAppLifecycle", "onActivityDestroyed: " + activity.javaClass.name)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log.d("ReadingAppLifecycle", "onActivitySaveInstanceState: " + activity.javaClass.name)
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d("ReadingAppLifecycle", "onActivityStopped: " + activity.javaClass.name)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d("ReadingAppLifecycle", "onActivityCreated: " + activity.javaClass.name)
        currentActivity = activity
    }
}