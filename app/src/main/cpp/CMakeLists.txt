cmake_minimum_required(VERSION 3.22.1)
project("nacl_test_jni")

# Add the includes from the SDK
include_directories(${CMAKE_SOURCE_DIR}/../../../../third_party/nacl-native-sdk/include)

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

# Create our JNI test library
add_library(nacl_test_jni SHARED nacl_test_jni.cpp)

# Link against the Android NDK libraries and the prebuilt SDK libraries
target_link_libraries(nacl_test_jni
    android_core
    native_host_bridge
    log
    android
)
