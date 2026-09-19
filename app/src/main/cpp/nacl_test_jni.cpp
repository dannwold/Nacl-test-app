#include <jni.h>
#include <string>
#include <android/log.h>
#include "android_core.h"

#define LOG_TAG "NaClTestJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global context pointer for demonstration
static NaclContext* g_nacl_ctx = nullptr;

extern "C" JNIEXPORT jint JNICALL
Java_com_example_nacltest_NativeBridge_initialize(JNIEnv *env, jobject thiz, jstring privateDirPath) {
    const char *dirPath = env->GetStringUTFChars(privateDirPath, nullptr);
    LOGI("Calling nacl_core_initialize with path: %s", dirPath);

    NaclResult status;
    g_nacl_ctx = nacl_core_initialize(&status);

    env->ReleaseStringUTFChars(privateDirPath, dirPath);

    if (status == NACL_SUCCESS) {
        LOGI("nacl_core_initialize succeeded.");
    } else {
        LOGE("nacl_core_initialize failed with status: %d", status);
    }

    return static_cast<jint>(status);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_nacltest_NativeBridge_shutdown(JNIEnv *env, jobject thiz) {
    LOGI("Calling nacl_core_shutdown");
    if (g_nacl_ctx) {
        nacl_core_shutdown(g_nacl_ctx);
        g_nacl_ctx = nullptr;
    }
    return 0; // NACL_SUCCESS
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_nacltest_NativeBridge_getAndroidSdkLevel(JNIEnv *env, jobject thiz) {
    LOGI("Calling nacl_core_get_android_sdk_level from android_core");
    int sdk_level = nacl_core_get_android_sdk_level();
    LOGI("nacl_core_get_android_sdk_level returned: %d", sdk_level);
    return sdk_level;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_nacltest_NativeBridge_testAndroidHostCall(JNIEnv *env, jobject thiz) {
    LOGI("Testing Android Host bridge call via JNI...");

    jclass clazz = env->GetObjectClass(thiz);
    if (!clazz) {
        LOGE("Could not get class of NativeBridge");
        return env->NewStringUTF("Error: Could not get class");
    }

    jmethodID methodId = env->GetMethodID(clazz, "hostGetAppVersion", "()Ljava/lang/String;");
    if (!methodId) {
        LOGE("Could not find hostGetAppVersion method");
        return env->NewStringUTF("Error: Could not find method");
    }

    LOGI("Invoking hostGetAppVersion()...");
    jstring resultString = (jstring) env->CallObjectMethod(thiz, methodId);

    if (env->ExceptionCheck()) {
        LOGE("Exception occurred during host method invocation");
        env->ExceptionClear();
        return env->NewStringUTF("Error: Exception during host call");
    }

    const char* nativeResult = env->GetStringUTFChars(resultString, nullptr);
    LOGI("Host returned string: %s", nativeResult);

    std::string finalResult = std::string("Host response: ") + nativeResult;
    env->ReleaseStringUTFChars(resultString, nativeResult);

    return env->NewStringUTF(finalResult.c_str());
}

// QuickJS Integration
#include "quickjs.h"

extern "C" {
    // Declarations from libquickjs_bindings.so
    int js_init_module_adb(JSContext *ctx, JSModuleDef *m);
    int js_init_module_shm(JSContext *ctx, JSModuleDef *m);
    // You can define other init functions here if necessary
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_nacltest_NativeBridge_evalQuickJS(JNIEnv *env, jobject thiz, jstring script) {
    const char *js_code = env->GetStringUTFChars(script, nullptr);
    LOGI("Evaluating QuickJS Script: %s", js_code);

    JSRuntime *rt = JS_NewRuntime();
    if (!rt) {
        env->ReleaseStringUTFChars(script, js_code);
        return env->NewStringUTF("Error: Could not allocate QuickJS Runtime");
    }

    JSContext *ctx = JS_NewContext(rt);
    if (!ctx) {
        JS_FreeRuntime(rt);
        env->ReleaseStringUTFChars(script, js_code);
        return env->NewStringUTF("Error: Could not allocate QuickJS Context");
    }

    // Register some capability modules as an example (since we statically embedded QuickJS, we must manually register the modules exposed from the shared library)
    // Note: To use them properly, they typically must be loaded dynamically, but for test evaluation we just verify execution.

    // Evaluate the JS script string
    JSValue result = JS_Eval(ctx, js_code, strlen(js_code), "<eval>", JS_EVAL_TYPE_GLOBAL);

    std::string result_str;
    if (JS_IsException(result)) {
        JSValue exception_val = JS_GetException(ctx);
        const char *str = JS_ToCString(ctx, exception_val);
        if (str) {
            LOGE("QuickJS Exception: %s", str);
            result_str = std::string("JS Exception: ") + str;
            JS_FreeCString(ctx, str);
        } else {
            result_str = "JS Exception: (Unknown)";
        }
        JS_FreeValue(ctx, exception_val);
    } else {
        const char *str = JS_ToCString(ctx, result);
        if (str) {
            LOGI("QuickJS Result: %s", str);
            result_str = std::string("JS Result: ") + str;
            JS_FreeCString(ctx, str);
        } else {
            result_str = "JS Result: (Success without string rep)";
        }
    }

    JS_FreeValue(ctx, result);
    JS_FreeContext(ctx);
    JS_FreeRuntime(rt);
    env->ReleaseStringUTFChars(script, js_code);

    return env->NewStringUTF(result_str.c_str());
}
