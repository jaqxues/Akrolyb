package com.jaqxues.akrolyb.sample

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.jaqxues.akrolyb.prefs.PrefManager
import com.jaqxues.akrolyb.sample.prefs.Preferences
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 27.03.20 - Time 20:35.
 */
const val TARGET_PKG_NAME = "com.jaqxues.akrolyb.sample.target"
const val TARGET_ACTIVITY_NAME = "com.jaqxues.akrolyb.sample.target.MainActivity"

class HookManager : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != TARGET_PKG_NAME)
            return

        // Prevents System APK hooking into Virtual Xposed
        if (HookManager::class.java.classLoader!!.toString()
                .contains("io.va.exposed") != (System.getProperty("vxp") != null)
        )
            return

        // Make sure app has permissions to read from External Storage
        val throwable = try {
            PrefManager.init(
                File(Environment.getExternalStorageDirectory(), "SomeFile.json"), Preferences::class
            )
            null
        } catch (t: Throwable) {
            Log.e("AkrolybSample", "Stage 1 Loading Error occurred", t)
            t
        }

        findAndHookMethod(
            Application::class.java,
            "attach",
            Context::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {

                    findAndHookMethod(
                        "com.jaqxues.akrolyb.sample.target.Utils", lpparam.classLoader,
                        "hookTarget", XC_MethodReplacement.returnConstant(true))


                    findAndHookMethod(TARGET_ACTIVITY_NAME, lpparam.classLoader,
                        "onCreate", Bundle::class.java, object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {

                                callMethod(param.thisObject, "testInvokeMethod")
                                if (throwable != null) {
                                    callMethod(param.thisObject, "showErrorMsg", throwable.message)
                                    return
                                }

                                try {
//                                    Provider.features.lateInitAll(lpparam.classLoader, param.thisObject as Activity)
                                } catch (t: Throwable) {
                                    Log.e("AkrolybSample", "Stage 3 Loading Error occurred", t)
                                }
                            }
                        })

                    try {
//                    Provider.features.loadAll(
//                        lpparam.classLoader,
//                        param.args[0] as Context
//                    )
                    } catch (t: Throwable) {
                        Log.e("AkrolybSample", "Stage 2 Loading Error occurred", t)
                    }
                }
            })
    }
}