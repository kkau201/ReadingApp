package com.example.readingapp.common

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DependencyContextWrapper @Inject constructor(
    @ApplicationContext val context: Context
)