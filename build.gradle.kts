// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.5.0" apply false
    id("com.google.devtools.ksp") version "2.3.10" apply false
    id("com.google.dagger.hilt.android") version "2.60.1" apply false

    // @Serializable 기반 타입세이프 네비게이션을 위해 serialization 플러그인 추가
    alias(libs.plugins.kotlin.serialization) apply false
}