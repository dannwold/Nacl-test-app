plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.nacltest"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nacltest"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
            }
        }

        ndk {
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    sourceSets {
        getByName("main") {
            // Include extracted libraries
            jniLibs.srcDir("../third_party/nacl-native-sdk/jniLibs")
        }
    }
}

// Add an unzip task to extract the SDK before the build starts
tasks.register<Copy>("unzipNaclSdk") {
    val zipFile = file("../nacl-native-sdk.zip")
    val outputDir = file("../third_party/nacl-native-sdk")

    from(zipTree(zipFile))
    into(outputDir)

    // Only run if the zip exists
    onlyIf { zipFile.exists() }
}

// Ensure unzip happens before C++ builds
tasks.whenTaskAdded {
    if (name.startsWith("configureCMake")) {
        dependsOn("unzipNaclSdk")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}

tasks.whenTaskAdded {
    if (name.startsWith("merge") && name.endsWith("JniLibFolders")) {
        dependsOn("unzipNaclSdk")
    }
}
