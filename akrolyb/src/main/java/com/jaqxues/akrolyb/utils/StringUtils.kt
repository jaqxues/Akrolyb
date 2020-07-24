package com.jaqxues.akrolyb.utils

import java.text.SimpleDateFormat
import java.util.*


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 12.05.20 - Time 17:09.
 */
object StringUtils {
    val HHmmssSSS = SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH)
    val ddMMyyyy = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    val yyyyMMdd = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
}
val Date.HHmmssSSS: String
    get() = StringUtils.HHmmssSSS.format(this)
val Date.yyyyMMdd: String
    get() = StringUtils.yyyyMMdd.format(this)