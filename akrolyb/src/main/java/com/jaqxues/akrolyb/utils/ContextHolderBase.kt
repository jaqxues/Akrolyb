package com.jaqxues.akrolyb.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber
import java.lang.ref.WeakReference

/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 10.02.21 - Time 18:23.
 */
abstract class ContextHolderBase {
    private var moduleContextRef: WeakReference<Context>? = null
    private var activityRef: WeakReference<Activity>? = null
    
    protected abstract val packageName: String

    var activity: Activity?
        get() = activityRef?.get()
        set(value) {
            activityRef = WeakReference(value)
        }

    fun setModuleContext(moduleContext: Context) {
        moduleContextRef = WeakReference(moduleContext)
    }

    fun getModuleContext(appContext: Context? = null): Context? {
        var moduleContext = moduleContextRef?.get()
        if (moduleContext == null) {
            appContext?.let { ctx ->
                moduleContext = createModuleContext(ctx)
            }
        }
        return moduleContext
    }

    fun getModuleContextNotNull(appContext: Context? = null) = getModuleContext(appContext)
            ?: throw IllegalStateException("Module Context is null")

    fun createModuleContext(appContext: Context): Context {
        try {
            return appContext.createPackageContext(
                    packageName, Context.CONTEXT_IGNORE_SECURITY
            )
        } catch (e: PackageManager.NameNotFoundException) {
            val i = IllegalStateException("Could not create Package Context", e)
            Timber.e(i)
            throw i
        }
    }
}