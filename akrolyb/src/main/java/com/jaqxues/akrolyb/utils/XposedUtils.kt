package com.jaqxues.akrolyb.utils

import androidx.annotation.Keep
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import timber.log.Timber


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnipTools.<br>
 * Date: 04.06.20 - Time 11:03.
 *
 * Moved to Akrolyb on
 * Date 26.09.20 - Time 21:55.
 */
const val UNAVAILABLE = -1

fun XC_MethodHook.MethodHookParam.invokeOriginalMethod(): Any? =
    XposedBridge.invokeOriginalMethod(method, thisObject, args)

object XposedUtils {
    var loadedXposedVersion = UNAVAILABLE
        private set
    var loadedAppVersion = UNAVAILABLE
        private set
    val isHooked get() = loadedXposedVersion != UNAVAILABLE

    @Keep
    @JvmStatic
    private fun hookTarget(xposedVersion: Int, appVersion: Int) {
        loadedXposedVersion = xposedVersion
        loadedAppVersion = appVersion
    }

    fun applySelfHooks(classLoader: ClassLoader, appVersion: Int) {
        check(appVersion != UNAVAILABLE)

        try {
            XposedHelpers.callStaticMethod(
                    classLoader.loadClass(XposedUtils::class.java.name),
                    XposedUtils::hookTarget.name,
                    XposedBridge.getXposedVersion(), appVersion
            )
        } catch (t: Throwable) {
            val message = when (t) {
                is NoSuchMethodError -> "Target for self hook method has not been found, reboot is recommended"
                is XposedHelpers.InvocationTargetError -> "Could not execute target method properly"
                else -> "Failed to setup self hooks"
            }
            Timber.e(t, message)
        }
    }
}