package com.jaqxues.akrolyb.logger

import android.util.Log
import com.jaqxues.akrolyb.utils.HHmmssSSS
import java.util.*


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 12.05.20 - Time 19:20.
 */
data class LogItem(val priority: Int, val tag: String?, val message: String, val t: Throwable?, val time: Long = System.currentTimeMillis()) {
    fun writeInLogFormat(appendable: Appendable) {
        appendable.apply {
            append(" [ ")
            append(Date(time).HHmmssSSS)
            append(" ] - [ ")
            append(
                when (priority) {
                    Log.ASSERT -> "ASSERT"
                    Log.ERROR -> "ERROR"
                    Log.WARN -> "WARN"
                    Log.INFO -> "INFO"
                    Log.DEBUG -> "DEBUG"
                    Log.VERBOSE -> "VERBOSE"
                    else -> throw IllegalArgumentException("Priority $priority not in range ${Log.VERBOSE}..${Log.ASSERT}")
                }
            )
            append(" ] - ")
            tag?.let {
                append("[ ")
                append(it)
                append(" ] - ")
            }
            append(message)

            t?.let {
                append("\n")
                append(Log.getStackTraceString(it))
            }

            append("\n\n")
        }
    }
}