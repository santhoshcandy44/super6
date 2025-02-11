import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    // Add the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.super6.pot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.super6.pot"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String" ,"DEBUG_BASE_URL","\"http://192.168.145.85:3000\"")
        buildConfigField("String" ,"BASE_URL","\"http://15.207.14.2:3000\"")
        buildConfigField("String" ,"DEBUG_SOCKET_BASE_URL","\"http://192.168.145.85:3080\"")
        buildConfigField("String" ,"SOCKET_BASE_URL","\"http://15.207.14.2:3080\"")
        buildConfigField("String" ,"REFERER","\"com.super6.pot.referer\"")

        vectorDrawables {
            useSupportLibrary = true
        }

        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }

        ndk {
            // Configure the NDK version if needed
            version = "28.0.12674087"  // Choose the version that matches your setup
        }


    }

    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt") // CMake build file path
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }



    composeCompiler {

        reportsDestination = layout.buildDirectory.dir("compose_compiler")
        stabilityConfigurationFile =
            rootProject.layout.projectDirectory.file("stability_config.conf")
    }


    packaging {
        // Exclude unnecessary resources like licenses
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }

        // Force DEX files to be compressed (reduce APK size)
        dex {
            useLegacyPackaging = true  // Compress DEX files
        }

        // Force native libraries to be compressed (reduce APK size)
        jniLibs {
            useLegacyPackaging = true  // Compress native libraries
        }
    }



    ndkVersion = "28.0.12674087 rc2"


}

dependencies {



    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx) // Latest version

    implementation(libs.androidx.navigation.compose)


    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.material)
    implementation(libs.material3)
    implementation(libs.androidx.material.icons.extended) // or latest version

    // Coil for image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.gif)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging) // Check for the latest version
    implementation(libs.firebase.analytics) // or the latest version

    implementation(libs.androidx.credentials)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.location)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp.urlconnection)

    implementation(libs.socket.io.client)

    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.room.compiler)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.room.compiler)
    implementation(libs.androidx.room.paging)


    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp(libs.dagger.hilt.compiler)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)


    implementation(libs.androidx.paging.runtime)
    testImplementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)



    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)


    implementation(libs.androidx.camera.core)
    implementation( libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.androidx.exifinterface)

    implementation(libs.androidx.security.crypto)

    implementation(libs.androidx.browser) // Add the latest version of browser dependency

    implementation(libs.places)

    implementation(libs.jsoup)  // Use the latest stable version

}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

