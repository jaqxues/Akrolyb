package com.jaqxues.akrolyb.genhook

import android.app.Activity
import android.content.Context


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 16.03.20 - Time 15:34.
 */
interface Feature {
    fun loadFeature(classLoader: ClassLoader, context: Context)

    fun lateInit(classLoader: ClassLoader, activity: Activity)
}