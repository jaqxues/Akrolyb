package com.jaqxues.akrolyb.logger

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import com.jaqxues.akrolyb.utils.yyyyMMdd
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project Akrolyb.<br>
 * Date: 12.05.20 - Time 12:35.
 */
const val TAG = "Akrolyb/FileLogger"

@SuppressLint("LogNotTimber")
open class FileLogger(val file: File) : Timber.Tree() {
    private val job = SupervisorJob()
    private val channel = Channel<LogItem>(Channel.UNLIMITED)
    private var lastWritten: Long = -1

    init {
        file.createNewFile()
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (channel.trySend(LogItem(priority, tag, message, t)).isFailure) {
            Log.wtf(TAG, IllegalStateException("Could not send an error to the Channel."))
        }
    }

    private suspend fun CoroutineScope.receiveAndWrite() {
        FileWriter(file, true).buffered().use { writer ->
            try {
                while (isActive) {
                    if (System.currentTimeMillis() - lastWritten > debugInterval)
                        appendDebugData(writer)
                    channel.receive().writeInLogFormat(writer)
                    writer.flush()
                    lastWritten = System.currentTimeMillis()
                }
                channel.close()
                Log.i(TAG, "Logging Coroutine was cancelled. Processing the rest of the items in the Channel")
                var item: LogItem?
                while (channel.tryReceive().also { item = it.getOrNull() }.isSuccess) {
                    writeLogItem(writer, item!!)
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
                """ |
|                   |Debug Data Report:
                    |OS Version: ${Build.VERSION.SDK_INT} - ${Build.VERSION.CODENAME}
                    |
                    |Additional Debug Data:
                """.trimMargin())
            append(debugData)
        }
    }

    protected open val debugData: String = ""
    protected open val debugInterval: Long = TimeUnit.HOURS.toMillis(1)
    protected open fun writeLogItem(appendable: Appendable, logItem: LogItem) = logItem.writeInLogFormat(appendable)

    fun stop() {
        job.cancel()
    }

    companion object {
        fun getLogFile(folder: File, throwLogs: Int = 20): File {
            if (!folder.exists()) throw IllegalArgumentException("Folder does not exist")

            if (throwLogs > 0) {
                val logFiles = folder.listFiles() ?: emptyArray()
                if (logFiles.size > throwLogs) {
                    var toDelete = logFiles.size - throwLogs
                    logFiles.sortedBy { it.lastModified() }.forEach {
                        if (toDelete <= 0) return@forEach
                        it.delete()
                        toDelete -= 1
                    }
                }
            }
            return File(folder, "log_${Date(System.currentTimeMillis()).yyyyMMdd}.txt")
        }
    }
}
