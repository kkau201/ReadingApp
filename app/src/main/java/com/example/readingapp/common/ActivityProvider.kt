package com.example.readingapp.common

import android.app.Activity
import androidx.fragment.app.FragmentActivity

interface ActivityProvider {

    fun currentActivity(): Activity?

    fun currentFragmentActivity(): FragmentActivity?

}