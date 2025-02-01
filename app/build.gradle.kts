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

        buildConfigField("String" ,"BASE_URL","\"http://192.168.41.85:3000\"")
        buildConfigField("String" ,"SOCKET_BASE_URL","\"http://192.168.41.85:3080\"")
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
        viewBinding = true
        compose = true
        buildConfig = true

    }



    composeCompiler {

        reportsDestination = layout.buildDirectory.dir("compose_compiler")
        stabilityConfigurationFile =
            rootProject.layout.projectDirectory.file("stability_config.conf")
    }



    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    ndkVersion = "28.0.12674087 rc2"


}

dependencies {


    implementation(libs.play.services.location)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.hilt.work)
    implementation(libs.googleid)


    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7") // Latest version

    implementation("androidx.compose.material:material-icons-extended:1.7.6") // or latest version



    implementation("androidx.exifinterface:exifinterface:1.3.7")

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
    implementation(libs.material3)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.activity.compose)


    implementation(libs.androidx.lifecycle.viewmodel.compose) // Latest version

    // Navigation for Compose
    implementation(libs.androidx.navigation.compose) // Latest version

    // Coil for image loading
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
    implementation("io.coil-kt.coil3:coil-gif:3.0.4")

    implementation("androidx.compose.material:material:1.7.6")


    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-crashlytics")

    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.2.0")


    // Add Retrofit dependency
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

// Add Gson converter for JSON serialization/deserialization (optional)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Add OkHttp logging interceptor for logging HTTP requests and responses (optional)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.9.1")

    /*
        implementation ("com.github.bumptech.glide:glide:4.12.0")
        annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    */

    implementation("io.socket:socket.io-client:2.0.0")


    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.room.compiler)

    implementation(libs.androidx.room.paging)

    // To use Kotlin Symbol Processing (KSP)
    ksp(libs.androidx.room.room.compiler)

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)



    implementation(libs.firebase.messaging) // Check for the latest version
    implementation(libs.firebase.analytics) // or the latest version

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp(libs.dagger.hilt.compiler)

    // When using Kotlin.
    ksp("androidx.hilt:hilt-compiler:1.2.0")


    // Jetpack Compose
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")


    implementation("androidx.credentials:credentials:1.5.0-beta01")

    // optional - needed for credentials support from play services, for devices running
    // Android 13 and below.

    // DataStore preferences dependency


    // For AndroidX Security library (needed for Encrypted DataStore)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation("androidx.browser:browser:1.8.0") // Add the latest version of browser dependency
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    val paging_version = "3.3.2"

    implementation("androidx.paging:paging-runtime:$paging_version")

    // alternatively - without Android dependencies for tests
    testImplementation("androidx.paging:paging-common:$paging_version")

    // optional - Jetpack Compose integration
    implementation("androidx.paging:paging-compose:3.3.5")
    implementation("com.google.android.libraries.places:places:4.1.0")

    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("androidx.media3:media3-common:1.5.1")


    implementation("androidx.camera:camera-core:1.4.1")
    implementation( "androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    implementation("org.jsoup:jsoup:1.15.4")  // Use the latest stable version

}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

