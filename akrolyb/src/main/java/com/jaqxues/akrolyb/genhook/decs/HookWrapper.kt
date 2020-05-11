package com.jaqxues.akrolyb.genhook.decs

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import timber.log.Timber


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 17.03.20 - Time 10:49.
 */


/**
 * Callback Wrapper for error handling and universal debugging purposes
 */
abstract class HookWrapper : XC_MethodHook() {
    @Throws(Throwable::class)
    final override fun beforeHookedMethod(param: MethodHookParam) {
        try {
            before(param)
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    @Throws(Throwable::class)
    final override fun afterHookedMethod(param: MethodHookParam) {
        try {
            after(param)
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    @Throws(Throwable::class)
    protected open fun before(param: MethodHookParam) {
    }

    @Throws(Throwable::class)
    protected open fun after(param: MethodHookParam) {
    }
}

abstract class ReplaceWrapper : XC_MethodReplacement() {
    @Throws(Throwable::class)
    final override fun replaceHookedMethod(param: MethodHookParam): Any? =
            try {
                replace(param)
            } catch (ex: Exception) {
                Timber.e(ex)
            }


    @Throws(Throwable::class)
    protected abstract fun replace(param: MethodHookParam): Any?
}