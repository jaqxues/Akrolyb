package com.jaqxues.akrolyb.prefs.gson

import com.google.gson.*
import com.jaqxues.akrolyb.prefs.Preference
import com.jaqxues.akrolyb.prefs.PreferenceMap
import com.jaqxues.akrolyb.prefs.PreferenceMapSerializer
import com.jaqxues.akrolyb.prefs.strictMode
import timber.log.Timber
import java.io.Reader
import java.io.Writer
import java.lang.reflect.Type


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 15.03.20 - Time 21:44.
 */
class GsonPrefMapSerializer: JsonDeserializer<PreferenceMap>, JsonSerializer<PreferenceMap>, PreferenceMapSerializer {
    private val gson = GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .registerTypeHierarchyAdapter(Map::class.java, this)
            .create()

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): PreferenceMap {
        val map = PreferenceMap()
        val rootArray = json as? JsonArray

        if (rootArray == null) {
            Timber.e("Malformed Preference Json: $json")
            return map
        }

        for (obj in rootArray) {
            try {
                obj as JsonObject
                val prefKey = obj.get("name").asString
                val value = obj.get("value")
                    ?: throw IllegalStateException("Preference JsonObject does not contain a \"value\" member.")
                val serialized = try {
                    val pref = Preference.findPrefByKey<Any>(prefKey)
                    GsonUtils.singleton.fromJson(value, pref.type) ?: PreferenceMap.OBJ_NULL
                } catch (ex: Exception) {
                    Timber.w(ex)
                    if (strictMode) continue

                    value
                }

                map[prefKey] = serialized
            } catch (e: Exception) {
                Timber.e(e, "Error de-serializing PreferenceMap ($obj)")
            }
        }
        return map
    }


    override fun serialize(src: PreferenceMap, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val array = JsonArray()
        val copy = PreferenceMap(src)
        for ((key, v) in copy.entries) {
            var value = v
            if (value === PreferenceMap.OBJ_NULL)
                value = null
            // Ignore unloaded values if in strict mode (might delete used preferences)
            if (value is JsonElement && strictMode) continue

            try {
                array.add(JsonObject().apply {
                    addProperty("name", key)
                    add("value", if (value is JsonElement) value else GsonUtils.singleton.toJsonTree(value))
                })
            } catch (e: Exception) {
                Timber.e(e, "Error serializing PreferenceMap ($key - $value")
            }
        }
        return array
    }

    override fun serialize(writer: Writer, map: PreferenceMap) {
        gson.toJson(map, writer)
    }

    override fun deserialize(reader: Reader): PreferenceMap? {
        return gson.fromJson(reader, PreferenceMap::class.java)
    }
}