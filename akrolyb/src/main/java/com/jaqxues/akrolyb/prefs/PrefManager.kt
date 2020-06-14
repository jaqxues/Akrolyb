package com.jaqxues.akrolyb.prefs

import com.jaqxues.akrolyb.prefs.gson.GsonPrefMapSerializer
import com.jaqxues.akrolyb.utils.createFile
import timber.log.Timber
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 15.03.20 - Time 21:38.
 */

private val FILE_LOCK = Any()

/**
 * StrictMode: If enabled, it ignores Preferences that are not loaded
 * Dangerous: Risk of deleting used Preferences by overwriting the file and ignoring unloaded Preferences
 */
internal var strictMode: Boolean = false
private lateinit var prefMap: PreferenceMap
private lateinit var prefObserver: PrefObserver
private lateinit var prefFile: File
private lateinit var prefClasses: Array<out KClass<*>>

object PrefManager {
    private val isInitialized = AtomicBoolean()

    lateinit var serializer: PreferenceMapSerializer
        private set

    fun init(file: File, vararg preferences: KClass<*>, serializer: PreferenceMapSerializer = GsonPrefMapSerializer()): Boolean {
        if (isInitialized.getAndSet(true)) {
            Timber.w("Already initialized PrefManager")
            return true
        }

        if (preferences.isEmpty()) {
            Timber.e("Called init with no Preferences Class")
            return false
        }

        prefClasses = preferences

        this.serializer = serializer
        synchronized(FILE_LOCK) {
            file.createFile()?.let {
                prefFile = it

                Timber.d("Preferences File Path: ${it.absolutePath}")

                prefObserver = PrefObserver(prefFile)
                prefObserver.startWatching()

                triggerReload()

                return true
            }
        }

        isInitialized.set(false)
        throw IllegalStateException("Preferences: Initialization Failed")
    }

    fun addPreferences(vararg preferences: KClass<*>, _strictMode: Boolean = true) {
        strictMode = _strictMode
        triggerReload(*preferences)
    }

    /**
     * @param preferences Allow loading only new preferences, defaults to all classes ([prefClasses])
     */
    private fun triggerReload(vararg preferences: KClass<*> = prefClasses) {
        Preference.collectPreferences(*preferences)
        loadPrefMap()
    }

    @JvmStatic
    fun isInitialized() = isInitialized.get()

    @JvmStatic
    fun forceReset() {
        synchronized(FILE_LOCK) {
            prefMap.clear()
            prefObserver.notifyLocalChange()
            prefMap.saveMap(prefFile)
        }
    }

    fun loadPrefMap() {
        synchronized(FILE_LOCK) {
            try {
                prefMap = PreferenceMap.fromPrefFile(prefFile)
            } catch (e: Exception) {
                Timber.e(e, "Failed to load PreferenceMap")
            }
        }
    }
}


/*
 === Extension functions for Preferences that need direct access to the PrefMap ===
 Other Helper functions for working with a Preference Object are in the Preference class.
 */
@Suppress("UNCHECKED_CAST")
fun <T> Preference<T>.getPref() =
        (synchronized(this) {
            prefMap[key]
        } ?: default) as T

/**
 * Returns previous value
 */
@Suppress("UNCHECKED_CAST")
fun <T> Preference<out T>.putPref(value: T): T? {
    return synchronized(this) {
        val previous = prefMap.put(key, value) as T?
        if (previous == null || previous != value) {
            prefObserver.notifyLocalChange()
            prefMap.saveMap(prefFile)
        }
        previous ?: default
    }
}

/**
 * Returns previous value
 */
@Suppress("UNCHECKED_CAST")
fun <T> Preference<T>.removePref(): T? {
    return synchronized(this) {
        val previous = prefMap.remove(key) as T?
        if (previous != null) {
            prefObserver.notifyLocalChange()
            prefMap.saveMap(prefFile)
        }
        previous
    }
}
