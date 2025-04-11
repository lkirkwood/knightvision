plugins {
    id("com.android.application")
    kotlin("android") version "2.1.20"
}

android {
    namespace = "com.knightvision"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.knightvision"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}
