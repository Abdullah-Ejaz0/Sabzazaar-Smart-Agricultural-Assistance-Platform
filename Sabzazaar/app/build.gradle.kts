import java.net.NetworkInterface
import java.util.Collections

plugins {
    alias(libs.plugins.android.application)
}

fun getLocalIpAddress(): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        val list = Collections.list(interfaces)
        for (networkInterface in list) {
            if (networkInterface.isLoopback || !networkInterface.isUp) continue
            val addresses = networkInterface.inetAddresses
            for (address in Collections.list(addresses)) {
                if (address is java.net.Inet4Address) {
                    val host = address.hostAddress
                    // Prioritize standard local network ranges (WiFi/Ethernet)
                    if (host.startsWith("192.168.") || host.startsWith("10.0.")) {
                        return host
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "10.0.2.2" // Fallback
}

android {
    namespace = "com.example.sabzazaar"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.sabzazaar"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val currentIp = getLocalIpAddress()
        buildConfigField("String", "BASE_URL", "\"http://$currentIp:8000/\"")
    }

    buildFeatures {
        buildConfig = true
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // CameraX dependencies
    val cameraxVersion = "1.4.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    // Glide for image loading (scan thumbnails, community photos)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}