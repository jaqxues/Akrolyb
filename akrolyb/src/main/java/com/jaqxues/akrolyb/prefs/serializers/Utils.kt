package com.jaqxues.akrolyb.prefs.serializers

import com.google.gson.Gson
import com.jaqxues.akrolyb.prefs.Types
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import com.google.gson.JsonElement as GJsonElement


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 15.03.20 - Time 21:49.
 */

fun Gson.fromJson(el: GJsonElement, type: Types) =
    when (type) {
        is Types.KClassType -> fromJson(el, type.type.java)
        is Types.ReflectType -> fromJson(el, type.type)
        else -> error("Unsupported Type, Incompatible Serializer")
    }

@OptIn(InternalSerializationApi::class)
val Types.serializer
    get() =
        when (this) {
            is Types.KClassType -> type.serializer()
            is Types.KTypeType -> serializer(type)
            else -> error("Unsupported Type, Incompatible Serializer")
        }

fun <T> Json.encodeAnyToJsonElement(serializer: KSerializer<T>, obj: Any?) =
    encodeToJsonElement(serializer, obj as T)

object GsonUtils {
    val singleton by lazy { Gson() }
}
