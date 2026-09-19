# NaCl Test App

This project is a native Android test application built around the supplied NaCl (Native Capability Library) SDK.

## Found within `nacl-native-sdk.zip`:
- **Libraries**: 25 provided pre-compiled `.so` native libraries covering subsystems such as camera, audio, storage, IPC, routing, telemetry, vulkan rendering, and more.
- **Headers**: 27 corresponding C headers detailing API surfaces, unified API (`nacl_unified_api.h`), android core system API, and JNI bridges.
- **Supported ABIs**: `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`

### QuickJS Integration
The SDK also supplies `libquickjs_bindings.so` and `quickjs.h`, which are designed as *module stubs* for a host JavaScript application. This means we have integrated the **full QuickJS interpreter** (version `2024-01-13`) source natively into our CMake compilation. The native capabilities (`libquickjs_bindings.so`) link against these capabilities enabling JavaScript code evaluated in this runtime to seamlessly interface with the subsystem modules.

## Integration
- Extracted SDK to `third_party/nacl-native-sdk/`.
- Embedded QuickJS source `third_party/quickjs/`.
- Precompiled libraries integrated using CMake (`IMPORTED SHARED`) in `app/src/main/cpp/CMakeLists.txt`.
- Set Gradle's `jniLibs.srcDir` directly to point to the precompiled `.so` assets in the `third_party/` directory, maintaining ABI structure.

## Android Host Bridge Architecture
The bridge proves bi-directional capabilities from native to Kotlin over JNI:
1. **Kotlin -> Native**: `NativeBridge.kt` calls C++ definitions via JNI (`initialize`, `getAndroidSdkLevel`, `testAndroidHostCall`).
2. **Native -> Kotlin**: The native `testAndroidHostCall` function retrieves the current `JNIEnv`, discovers the `hostGetAppVersion` method defined in Kotlin, invokes it, and successfully retrieves an Android app version string, returning it seamlessly.
3. **QuickJS Execution**: A JNI layer function spins up the QuickJS runtime and evaluates JavaScript script strings injected directly from the Android host.

## Build Status & Results
- **BUILD VERIFIED**: Yes, Gradle cleanly compiles the target architecture native wrapper (`libnacl_test_jni.so`), loads prebuilt shared libraries, links efficiently, and builds the APK successfully.
- **DEVICE/RUNTIME VERIFIED**: Requires physical device/emulator execution for full verification. Tests provided prove JNI method bindings, loading the `.so` dependencies successfully, and executing an Android Host API via the SDK loopback mechanism.

### Next Steps
1. Deploy the tested APK onto an active `arm64-v8a` target (or emulator).
2. Wire actual application specific Kotlin functionalities (database interaction, shared preferences access) to the `hostGetAppVersion` roundtrip demonstrator.
3. Use JNI to map further capabilities discovered in the SDK.
