package com.example.nacltest

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler : Thread.UncaughtExceptionHandler {
    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        e.printStackTrace(pw)
        val stackTrace = sw.toString()

        Log.e("NaclCrashHandler", "FATAL CRASH: $stackTrace")

        // Pass it to default handler so app still crashes normally but we log it
        defaultHandler?.uncaughtException(t, e)
    }

    companion object {
        fun init() {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
            Log.i("NaclCrashHandler", "CrashHandler initialized")
        }
    }
}
