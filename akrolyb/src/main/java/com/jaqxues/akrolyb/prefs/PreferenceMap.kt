package com.jaqxues.akrolyb.prefs

import androidx.annotation.CheckResult
import com.jaqxues.akrolyb.prefs.PrefManager.serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.*
import java.util.concurrent.ConcurrentHashMap


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 15.03.20 - Time 21:24.
 */
private val FILE_LOCK = Any()

class PreferenceMap : ConcurrentHashMap<String, Any> {
    constructor()
    constructor(m: Map<String, Any>) : super(m)

    fun saveMap(prefFile: File) {
        GlobalScope.launch(Dispatchers.IO) {
            val copy = PreferenceMap(this@PreferenceMap)
            try {
                synchronized(FILE_LOCK) {
                    BufferedWriter(FileWriter(prefFile)).use {
                        serializer.serialize(it, copy)
                        it.flush()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    companion object {
        private const val serialVersionUID = 2162788535918724249L

        @CheckResult
        fun fromPrefFile(prefFile: File): PreferenceMap {
            if (!prefFile.exists())
                return PreferenceMap()
            return synchronized(FILE_LOCK) {
                BufferedReader(FileReader(prefFile)).use { reader ->
                    serializer.deserialize(reader)
                }
            } ?: PreferenceMap()
        }
    }
}

interface PreferenceMapSerializer {
    fun serialize(writer: Writer, map: PreferenceMap)
    fun deserialize(reader: Reader): PreferenceMap?
}
