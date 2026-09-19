package com.example.nacltest

import android.util.Log

class NativeBridge {

    interface HostCallback {
        fun getAppVersion(): String
    }

    private var callback: HostCallback? = null

    fun setHostCallback(cb: HostCallback) {
        this.callback = cb
    }

    // Called from Native code through JNI!
    // This proves the round-trip from Native to Android Host.
    fun hostGetAppVersion(): String {
        Log.d("NativeBridge", "Native code requested app version from host!")
        return callback?.getAppVersion() ?: "Unknown"
    }

    // JNI Native methods to interact with the NaCl SDK
    external fun initialize(privateDirPath: String): Int
    external fun testAndroidHostCall(): String
    external fun shutdown(): Int
    external fun getAndroidSdkLevel(): Int

    companion object {
        private const val TAG = "NativeBridge"

        init {
            // Load the provided SDK libraries required by libnative_host_bridge.so or our own library first,
            // depending on dependency graph.
            try {
                System.loadLibrary("android_core")
                System.loadLibrary("native_host_bridge")

                // Load our JNI test wrapper
                System.loadLibrary("nacl_test_jni")

                Log.i(TAG, "Native libraries loaded successfully.")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native libraries", e)
            }
        }
    }
}
