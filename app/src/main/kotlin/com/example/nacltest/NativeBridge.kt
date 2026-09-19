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

    // JNI Native method to evaluate JavaScript via QuickJS
    external fun evalQuickJS(script: String): String

    companion object {
        private const val TAG = "NativeBridge"

        init {
            // Load the provided SDK libraries required by libnative_host_bridge.so or our own library first,
            // depending on dependency graph.
            try {
                // Must load the deepest dependencies first
                System.loadLibrary("android_core")
                System.loadLibrary("routing_core")
                System.loadLibrary("ipc_crypto")
                System.loadLibrary("shm_client")
                System.loadLibrary("adb_client")

                System.loadLibrary("connectivity_automation") // depends on adb_client

                System.loadLibrary("sensors_client")
                System.loadLibrary("telephony_client")

                // mock_client_main depends on routing_core
                System.loadLibrary("mock_client_main")

                System.loadLibrary("bluetooth_client")
                System.loadLibrary("bluetooth_svc")

                System.loadLibrary("usb_subsystem")
                System.loadLibrary("camera_subsystem")
                System.loadLibrary("nfc_subsystem")
                System.loadLibrary("nacl_input")
                System.loadLibrary("nacl_audio")
                System.loadLibrary("nacl_location")
                System.loadLibrary("nacl_storage")
                System.loadLibrary("power_battery")

                System.loadLibrary("display_core")
                // display_jni_bridge depends on display_core
                System.loadLibrary("display_jni_bridge")
                System.loadLibrary("display_media")
                // vulkan_renderer depends on display_core
                System.loadLibrary("vulkan_renderer")

                System.loadLibrary("native_host_bridge")
                System.loadLibrary("quickjs_bindings")

                // Load our JNI test wrapper
                System.loadLibrary("nacl_test_jni")

                Log.i(TAG, "Native libraries loaded successfully.")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native libraries", e)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading native libraries", e)
            }
        }
    }
}
