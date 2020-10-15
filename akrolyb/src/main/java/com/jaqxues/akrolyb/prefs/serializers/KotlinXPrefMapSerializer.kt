package com.jaqxues.akrolyb.prefs.serializers

import com.google.gson.reflect.TypeToken
import com.jaqxues.akrolyb.prefs.Preference
import com.jaqxues.akrolyb.prefs.PreferenceMap
import com.jaqxues.akrolyb.prefs.PreferenceMapSerializer
import com.jaqxues.akrolyb.prefs.strictMode
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import timber.log.Timber
import java.io.Reader
import java.io.Writer


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 15.10.20 - Time 19:26.
 */
class KotlinXPrefMapSerializer : PreferenceMapSerializer {
    private val prettyFormatJson = Json { prettyPrint = true }

    override fun serialize(writer: Writer, map: PreferenceMap) {
        writer.use {
            it.write(buildJsonArray {
                for ((k, v) in map) {
                    // Ignore unloaded values if in strict mode (might delete used preferences)
                    if (v is JsonElement && strictMode) continue

                    // Allow null values
                    @Suppress("NAME_SHADOWING") var v = v
                    if (v === PreferenceMap.OBJ_NULL)
                        v = null

                    try {
                        add(buildJsonObject {
                            put("name", JsonPrimitive(k))
                            put(
                                "value",
                                if (v is JsonElement) v else prettyFormatJson.encodeToJsonElement(v)
                            )
                        })
                    } catch (e: Exception) {
                        Timber.e(e, "Error serializing PreferenceMap ($k, $v)")
                    }
                }
            }.toString())
        }
    }

    @OptIn(InternalSerializationApi::class)
    override fun deserialize(reader: Reader): PreferenceMap? {
        val map = PreferenceMap()

        val rootArray = try {
            Json.encodeToJsonElement(reader.readText()).jsonArray
        } catch (e: Error) {
            Timber.e("Malformed Preference Json")
            return map
        }

        for (obj in rootArray) {
            try {
                obj as JsonObject
                val prefKey = obj["name"]?.jsonPrimitive?.content ?: error("Null Preference Key")
                val value = obj["value"] ?: error("Null Value Property")

                val serialized = try {
                    val pref = Preference.findPrefByKey<Any?>(prefKey)
                    Json.decodeFromJsonElement(pref.type, value) ?: PreferenceMap.OBJ_NULL
                } catch (ex: Exception) {
                    Timber.w(ex)
                    if (strictMode) continue

                    value
                }

                map[prefKey] = serialized
            } catch (ex: Exception) {
                Timber.e("Error while deserializing PreferenceMap ($obj)")
            }
        }

        return map
    }
}

@OptIn(InternalSerializationApi::class)
fun main() {
    val pairJson = """
        { 
          "first": "a",
          "second": 0
        }
    """.trimIndent()
    println(Json.decodeFromString(serializer<Pair<String, Int>>(), pairJson))

    println(Json.decodeFromString(serializer(object : TypeToken<Pair<String, Int>>() {}.type), pairJson))
}