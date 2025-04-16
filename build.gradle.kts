// Top-level build file where you can add configuration options common to all sub-projects/modules.



plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false

    id("com.google.devtools.ksp") version "2.1.10-1.0.31" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.dagger.hilt.android") version "2.55" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0" apply false
    // Add the dependency for the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics") version "3.0.3" apply false
    id("com.android.library") version "8.9.1" apply false
}

buildscript {


    dependencies {
        classpath(libs.hilt.android.gradle.plugin)
        classpath(libs.symbol.processing.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)
    }
}

