package com.kaylakautai.inkle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

class ReadingActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    var currentActivity: Activity? = null

    override fun onActivityPaused(activity: Activity) {
        Log.d("InkleLifecycle", "onActivityPaused: " + activity.javaClass.name)
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d("InkleLifecycle", "onActivityResumed: " + activity.javaClass.name)
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        Log.d("InkleLifecycle", "onActivityStarted: " + activity.javaClass.name)
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d("InkleLifecycle", "onActivityDestroyed: " + activity.javaClass.name)
        // Don't hold on to a destroyed Activity (avoids leaking it across rotation etc.)
        if (currentActivity === activity) currentActivity = null
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log.d("InkleLifecycle", "onActivitySaveInstanceState: " + activity.javaClass.name)
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d("InkleLifecycle", "onActivityStopped: " + activity.javaClass.name)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d("InkleLifecycle", "onActivityCreated: " + activity.javaClass.name)
        currentActivity = activity
    }
}