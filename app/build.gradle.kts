plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.koalm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.koalm"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // - Imágenes
    implementation(libs.picasso)
    implementation(libs.coil.compose)

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // — Firebase & Firestore
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    //DataStorage
    implementation(platform("com.google.firebase:firebase-bom:32.1.1"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("io.coil-kt:coil-compose:2.4.0")







    // — Keyboard options
    implementation(platform("androidx.compose:compose-bom:2024.03.00"))
    implementation("androidx.compose.ui:ui-text")


    // — Google Sign‑In
    implementation(libs.play.services.auth)

    // — Credentials & Identity
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play)
    implementation(libs.googleid)

    // — Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // — Compose (BOM + UI modules)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.animation.core)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.runtime)
    implementation(libs.firebase.dataconnect)

    // — Testing
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.11.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //colores
    implementation("com.github.skydoves:colorpicker-compose:1.1.2")

    //Podometro
    implementation ("androidx.compose.runtime:runtime-livedata")



}

apply(plugin = "com.google.gms.google-services")