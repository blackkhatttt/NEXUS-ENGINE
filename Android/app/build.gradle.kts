plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.nexus.hub"
    compileSdk = 35
    ndkVersion = "27.0.12077973"
    defaultConfig {
        applicationId = "com.nexus.hub"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0"
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++20")
                targets += listOf("nexus_android")
            }
        }
    }
    externalNativeBuild {
        cmake {
            path = file("../../CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildTypes { release { isMinifyEnabled = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.documentfile:documentfile:1.0.1")
}
