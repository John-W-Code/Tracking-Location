import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.JW.trackinglocation"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.JW.trackinglocation"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val versionPropsFile = file("version.properties")
        val properties = Properties()
        if (versionPropsFile.exists()) {
            FileInputStream(versionPropsFile).use { properties.load(it) }
        }
        val buildNumber = (properties.getProperty("build.number") ?: "0").toInt() + 1
        properties.setProperty("build.number", buildNumber.toString())
        FileOutputStream(versionPropsFile).use { properties.store(it, null) }

        buildConfigField("String", "BUILD_NUMBER", "\"$buildNumber\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}