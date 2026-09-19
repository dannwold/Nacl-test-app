package com.example.nacltest

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity(), NativeBridge.HostCallback {

    private lateinit var tvStatus: TextView
    private lateinit var tvLogs: TextView
    private val nativeBridge = NativeBridge()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashHandler.init()
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvLogs = findViewById(R.id.tvLogs)

        nativeBridge.setHostCallback(this)

        logMessage("Architecture: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
        logMessage("Libraries loaded. Ready for initialization.")

        findViewById<Button>(R.id.btnInit).setOnClickListener {
            val privateDir = getDir("nacl_private", MODE_PRIVATE)
            val status = nativeBridge.initialize(privateDir.absolutePath)
            if (status == 0) { // NACL_STATUS_OK
                tvStatus.text = "Status: Initialized"
                logMessage("Initialize returned OK (0)")
            } else {
                tvStatus.text = "Status: Init Failed"
                logMessage("Initialize failed with code: $status")
            }
        }

        findViewById<Button>(R.id.btnSdkLevel).setOnClickListener {
            val sdkLevel = nativeBridge.getAndroidSdkLevel()
            logMessage("Native sdk level returned: $sdkLevel")
        }

        findViewById<Button>(R.id.btnTestHost).setOnClickListener {
            val result = nativeBridge.testAndroidHostCall()
            val jsResult = nativeBridge.evalQuickJS("10 + 20;")
            logMessage("QuickJS Eval Result: $jsResult")
            logMessage("Host Test Result: $result")
        }

        findViewById<Button>(R.id.btnShutdown).setOnClickListener {
            val status = nativeBridge.shutdown()
            tvStatus.text = "Status: Shutdown ($status)"
            logMessage("Shutdown returned code: $status")
        }
    }

    override fun getAppVersion(): String {
        return "v1.0 (from Kotlin Host)"
    }

    private fun logMessage(msg: String) {
        val currentLogs = tvLogs.text.toString()
        tvLogs.text = "$currentLogs\n$msg"
    }
}
