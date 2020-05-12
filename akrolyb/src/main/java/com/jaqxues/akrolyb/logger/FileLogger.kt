package com.jaqxues.akrolyb.logger

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import com.jaqxues.akrolyb.BuildConfig
import com.jaqxues.akrolyb.utils.ddMyyyy
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 12.05.20 - Time 12:35.
 */
private val WRITE_DEBUG_DATA_INTERVAL = TimeUnit.MINUTES.toMillis(20)
private const val MAX_LOG_FILES = 20
const val TAG = "Akrolyb/FileLogger"

@SuppressLint("LogNotTimber")
class FileLogger(val file: File) : Timber.Tree() {
    private val job = SupervisorJob()
    private val channel = Channel<LogItem>(Channel.UNLIMITED)
    private var lastWritten: Long = -1

    init {
        file.createNewFile()
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (!channel.offer(LogItem(priority, tag, message, t))) {
            Log.wtf(TAG, IllegalStateException("Could not send an error to the Channel."))
        }
    }

    private suspend fun CoroutineScope.receiveAndWrite() {
        FileWriter(file, true).buffered().use { writer ->
            try {
                while (isActive) {
                    if (System.currentTimeMillis() - lastWritten > WRITE_DEBUG_DATA_INTERVAL)
                        appendDebugData(writer)
                    channel.receive().writeInLogFormat(writer)
                    writer.flush()
                    lastWritten = System.currentTimeMillis()
                }
                channel.close()
                Log.i(TAG, "Logging Coroutine was cancelled. Processing the rest of the items in the Channel")
                var item: LogItem?
                while (channel.poll().also { item = it } != null) {
                    item!!.writeInLogFormat(writer)
                }
                writer.flush()
            } catch (e: IOException) {
                Log.wtf(TAG, "FileLogger failed to write items to log file", e)
                val ex = CancellationException("Failed to log items to file. Closing Logger", e)
                channel.cancel(ex)
                job.cancel(ex)
            }
        }
        Log.i(TAG, "Stopped Logging Coroutine")
    }

    fun startLogger(scope: CoroutineScope = GlobalScope) {
        scope.launch(job + Dispatchers.IO) {
            receiveAndWrite()
        }
    }

    private fun appendDebugData(appendable: Appendable) {
        appendable.apply {
            append(
                """ |Akrolyb Version: ${BuildConfig.VERSION_NAME}
                    |OS Version: ${Build.VERSION.SDK_INT} - ${Build.VERSION.CODENAME}
                """.trimMargin())
        }
    }

    fun stop() {
        job.cancel()
    }

    companion object {
        fun getInstance(folder: File, throwLogs: Boolean = true, autoStart: Boolean = true): FileLogger {
            if (!folder.exists()) throw IllegalArgumentException("Folder does not exist")

            if (throwLogs) {
                val logFiles = folder.listFiles() ?: emptyArray()
                if (logFiles.size > MAX_LOG_FILES) {
                    var toDelete = logFiles.size - MAX_LOG_FILES
                    logFiles.sortedBy { it.lastModified() }.forEach {
                        if (toDelete <= 0) return@forEach
                        it.delete()
                        toDelete -= 1
                    }
                }
            }
            val file = File(folder, "log_${Date(System.currentTimeMillis()).ddMyyyy}.txt")

            return FileLogger(file).apply { if(autoStart) startLogger() }
        }
    }
}
