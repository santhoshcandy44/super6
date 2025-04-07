plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
    namespace = "com.lts360"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lts360"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

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

        debug {
      /*      buildConfigField("String", "BASE_URL", "\"http://192.168.202.85:3000\"")
            buildConfigField("String", "SOCKET_BASE_URL", "\"http://192.168.202.85:3080\"")*/
            buildConfigField("String", "BASE_URL", "\"https://api.lts360.com\"")
            buildConfigField("String", "SOCKET_BASE_URL", "\"https://chat.lts360.com\"")
            buildConfigField("String", "REFERER", "\"referer.lts360.com\"")
            buildConfigField("String", "GOOGLE_SIGN_IN_OAUTH_WEB_CLIENT_ID", "\"300991981824-m4ovoojo09sklaqcvijandmnndduda0r.apps.googleusercontent.com\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://api.lts360.com\"")
            buildConfigField("String", "SOCKET_BASE_URL", "\"https://chat.lts360.com\"")
            buildConfigField("String", "REFERER", "\"referer.lts360.com\"")
            buildConfigField("String", "GOOGLE_SIGN_IN_OAUTH_WEB_CLIENT_ID", "\"300991981824-m4ovoojo09sklaqcvijandmnndduda0r.apps.googleusercontent.com\"")
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
        stabilityConfigurationFiles.addAll(
            rootProject.layout.projectDirectory.file("stability_config.conf")
        )
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


    // AndroidX Libraries
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("androidx.appcompat:appcompat:1.7.0")

    dependencies {
        // Testing Libraries
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)

        // Apply constraints for resolving duplicate versions
        implementation("androidx.test.espresso:espresso-core:3.6.1") {
            because("Resolving duplicate versions")
        }
    }


    implementation("androidx.multidex:multidex:2.0.1")

    // Jetpack Compose BOM
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.ui:ui:1.8.0-rc02")
    implementation("androidx.compose.ui:ui-test:1.8.0-rc02")
    implementation("androidx.compose.ui:ui-graphics:1.8.0-rc02")
    implementation("androidx.compose.ui:ui-tooling-preview:1.8.0-rc02")
    debugImplementation("androidx.compose.ui:ui-tooling:1.8.0-rc02")

    // Compose UI Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.8.0-rc02")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.8.0-rc02")

    // Activity and Lifecycle
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.8")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Material Design 3
    implementation("androidx.compose.material3:material3:1.4.0-alpha09")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Coil for Image Loading
    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")
    implementation("io.coil-kt.coil3:coil-gif:3.1.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    // Google Play Services

    implementation("androidx.credentials:credentials:1.5.0")
    // optional - needed for credentials support from play services, for devices running
    // Android 13 and below.
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")

    implementation("com.google.android.gms:play-services-location:21.3.0")

    //oAuth Google
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Retrofit and OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.12.0") // For JavaNetCookieJar

    // Socket.IO
    implementation("io.socket:socket.io-client:2.0.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-paging:2.6.1")

    // Hilt for Dependency Injection
    implementation("com.google.dagger:hilt-android:2.55")
    ksp("com.google.dagger:hilt-compiler:2.55")
    ksp("com.google.dagger:hilt-android-compiler:2.55")
// Hilt Navigation for Jetpack Compose
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Paging
    implementation("androidx.paging:paging-runtime-ktx:3.3.6")
    testImplementation("androidx.paging:paging-common-ktx:3.3.6")
    implementation("androidx.paging:paging-compose:3.3.6")

    // ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("androidx.media3:media3-common:1.5.1")

    // Camera
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-compose:1.5.0-alpha06")
    implementation("androidx.camera:camera-video:1.4.1" ) // For video recording
    implementation("androidx.camera:camera-effects:1.4.1")

    // ExifInterface
    implementation("androidx.exifinterface:exifinterface:1.4.0")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Browser
    implementation("androidx.browser:browser:1.8.0")

    // Places
    implementation("com.google.android.libraries.places:places:4.1.0")

    // Jsoup
    implementation("org.jsoup:jsoup:1.15.4")


    implementation("com.google.zxing:core:3.5.1")
    implementation("androidx.datastore:datastore-preferences:1.1.3")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("org.apache.commons:commons-math3:3.6.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

