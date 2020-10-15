package com.jaqxues.akrolyb.prefs.serializers

import com.google.gson.Gson
import com.google.gson.JsonElement as GJsonElement
import com.jaqxues.akrolyb.prefs.Types
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlinx.serialization.json.JsonElement as KJsonElement


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 15.03.20 - Time 21:49.
 */

fun Gson.fromJson(el: GJsonElement, type: Types) =
    when (type) {
        is Types.KClassType -> fromJson(el, type.type.java)
        is Types.ReflectType -> fromJson(el, type.type)
    }

@OptIn(InternalSerializationApi::class)
fun Json.decodeFromJsonElement(type: Types, el: KJsonElement) = Json.decodeFromJsonElement(
    when (type) {
        is Types.KClassType -> type.type.serializer()
        is Types.ReflectType -> serializer(type.type)
    }, el
)

object GsonUtils {
    val singleton by lazy { Gson() }
}
