// Top-level build file where you can add configuration options common to all sub-projects/modules.



plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.devtools.ksp") version "2.0.20-1.0.24" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.dagger.hilt.android") version "2.42" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.10" apply false
    // Add the dependency for the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}

buildscript {


    dependencies {
        classpath (libs.hilt.android.gradle.plugin)
        classpath (libs.com.google.devtools.ksp.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)

    }
}

