cmake_minimum_required(VERSION 3.22.1)
project("nacl_test_jni")

# QuickJS requires specific compile definitions
add_definitions(-DCONFIG_VERSION="2024-01-13" -DCONFIG_BIGNUM)

set(QUICKJS_DIR ${CMAKE_SOURCE_DIR}/../../../../third_party/quickjs)

# Add includes for QuickJS and SDK
include_directories(${QUICKJS_DIR})
include_directories(${CMAKE_SOURCE_DIR}/../../../../third_party/nacl-native-sdk/include)

# QuickJS Sources
set(QUICKJS_SOURCES
    ${QUICKJS_DIR}/quickjs.c
    ${QUICKJS_DIR}/libregexp.c
    ${QUICKJS_DIR}/libunicode.c
    ${QUICKJS_DIR}/cutils.c
    ${QUICKJS_DIR}/libbf.c
)

# Tell CMake where to find the prebuilt libraries for the current ABI
set(NACL_LIBS_DIR ${CMAKE_SOURCE_DIR}/../../../../third_party/nacl-native-sdk/jniLibs/${ANDROID_ABI})

# Prebuilt library: libandroid_core
add_library(android_core SHARED IMPORTED)
set_target_properties(android_core PROPERTIES
    IMPORTED_LOCATION ${NACL_LIBS_DIR}/libandroid_core.so
)

# Prebuilt library: libnative_host_bridge
add_library(native_host_bridge SHARED IMPORTED)
set_target_properties(native_host_bridge PROPERTIES
    IMPORTED_LOCATION ${NACL_LIBS_DIR}/libnative_host_bridge.so
)

# Prebuilt library: libquickjs_bindings (the capability modules that bind into QJS)
add_library(quickjs_bindings SHARED IMPORTED)
set_target_properties(quickjs_bindings PROPERTIES
    IMPORTED_LOCATION ${NACL_LIBS_DIR}/libquickjs_bindings.so
)

# Create our JNI test library including QuickJS sources
add_library(nacl_test_jni SHARED nacl_test_jni.cpp ${QUICKJS_SOURCES})

# Link against the Android NDK libraries and the prebuilt SDK libraries
target_link_libraries(nacl_test_jni
    android_core
    native_host_bridge
    quickjs_bindings
    log
    android
)
