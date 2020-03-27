package com.jaqxues.akrolyb.prefs.gson

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jaqxues.akrolyb.prefs.Types


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 15.03.20 - Time 21:49.
 */

fun <T: Any> Gson.fromJson(el: JsonElement, type: Types<T>): T =
        when (type) {
            is Types.KClassType -> fromJson(el, type.type.java)
            is Types.ReflectType -> fromJson(el, type.type)
        }

object GsonUtils {
    val singleton by lazy { Gson() }
}
