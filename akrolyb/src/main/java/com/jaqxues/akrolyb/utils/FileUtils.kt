package com.jaqxues.akrolyb.utils

import timber.log.Timber
import java.io.File


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Instaprefs.<br>
 * Date: 15.03.20 - Time 22:19.
 */

fun File.createFile(): File? {
    if (!exists()) {
        try {
            parentFile!!.mkdirs()
            createNewFile()
        } catch (e: Exception) {
            Timber.e(e)
            return null
        }
    }
    return this
}