package com.jaqxues.akrolyb.utils

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project SnipTools.<br>
 * Date: 04.06.20 - Time 11:03.
 *
 * Moved to Akrolyb on
 * Date 26.09.20 - Time 21:55.
 */
fun XC_MethodHook.MethodHookParam.invokeOriginalMethod(): Any? =
    XposedBridge.invokeOriginalMethod(method, thisObject, args)