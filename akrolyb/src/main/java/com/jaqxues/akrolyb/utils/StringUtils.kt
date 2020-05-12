package com.jaqxues.akrolyb.utils

import java.text.SimpleDateFormat
import java.util.*


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 12.05.20 - Time 17:09.
 */
object StringUtils {
    val HHmmssSSS = SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH)
    val ddMyyyy = SimpleDateFormat("dd-M-yyyy", Locale.ENGLISH)
}
val Date.HHmmssSSS: String
    get() = StringUtils.HHmmssSSS.format(this)
val Date.ddMyyyy: String
    get() = StringUtils.ddMyyyy.format(this)