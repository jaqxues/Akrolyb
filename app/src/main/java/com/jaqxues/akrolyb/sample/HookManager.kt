package com.jaqxues.akrolyb.sample

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Environment
import com.jaqxues.akrolyb.prefs.PrefManager
import com.jaqxues.akrolyb.sample.hooks.Declarations
import com.jaqxues.akrolyb.sample.hooks.Provider
import com.jaqxues.akrolyb.sample.prefs.Preferences
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 27.03.20 - Time 20:35.
 */

class HookManager : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.some.example.app")
            return

        // Prevents System APK hooking into Virtual Xposed
        if (HookManager::class.java.classLoader!!.toString().contains("io.va.exposed") != (System.getProperty("vxp") != null))
            return

        // Make sure app has permissions to read from External Storage
        PrefManager.init(
            File(Environment.getExternalStorageDirectory(), "SomeFile.json"), Preferences::class)

        findAndHookMethod(Application::class.java, "attach", Context::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    Provider.features.loadAll(
                        lpparam.classLoader,
                        param.args[0] as Context,
                        Declarations::class
                    )

                    findAndHookMethod("com.some.example.app.SomeActivity", lpparam.classLoader,
                        "onCreate", Bundle::class.java, object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                try {
                                    Provider.features.lateInitAll(lpparam.classLoader, param.thisObject as Activity)
                                } catch (t: Throwable) {
                                    XposedBridge.log(t)
                                }
                            }
                        })

                } catch (t: Throwable) {
                    XposedBridge.log(t)
                }
            }
        })
    }
}