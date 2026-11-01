plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.zyvix.zorinm4"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.zyvix.zorinm4"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0-mobile"
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
