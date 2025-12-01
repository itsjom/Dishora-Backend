import java.util.Properties

val properties = Properties()
if (rootProject.file("local.properties").isFile) {
    properties.load(rootProject.file("local.properties").inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.dishora"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dishora"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ Inject API keys from local.properties for both BuildConfig and Manifest
        val mapsKey = properties.getProperty("MAPS_API_KEY", "")
        val mapsMapId = properties.getProperty("MAPS_MAP_ID", "")

        manifestPlaceholders["MAPS_API_KEY"] = mapsKey
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")
        buildConfigField("String", "MAPS_MAP_ID", "\"$mapsMapId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Optional debug customizations
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
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
    implementation(libs.legacy.support.v4)
    implementation(libs.places)
    implementation(libs.play.services.maps)
    implementation(libs.preference)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.media3.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("me.relex:circleindicator:2.1.6")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.google.android.material:material:1.12.0")

    implementation("org.osmdroid:osmdroid-android:6.1.12")
    implementation("org.osmdroid:osmdroid-geopackage:6.1.10")
    implementation("com.android.volley:volley:1.2.1")
    implementation("org.json:json:20230227")

    implementation("androidx.activity:activity-ktx:1.6.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("org.apache.commons:commons-io:1.3.2")

    implementation("com.google.mlkit:text-recognition:16.0.1")

    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.40")

    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage:20.3.0")

    implementation("androidx.browser:browser:1.5.0")

    implementation ("com.microsoft.signalr:signalr:7.0.5")
    implementation ("org.slf4j:slf4j-android:1.7.32")
}