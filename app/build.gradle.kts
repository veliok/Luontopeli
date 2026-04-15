// app/build.gradle.kts
import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.luontopeli"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.luontopeli"
        minSdk = 26
        targetSdk = 35
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

    buildFeatures {
        compose = true
    }

    signingConfigs {
        create("release") {
            // Luetaan arvot local.properties -tiedostosta
            storeFile = file(localProperties.getProperty("KEYSTORE_PATH") ?: "")
            storePassword = localProperties.getProperty("KEYSTORE_PASSWORD") ?: ""
            keyAlias = localProperties.getProperty("KEY_ALIAS") ?: ""
            keyPassword = localProperties.getProperty("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            // Käytä release-allekirjoitusta
            signingConfig = signingConfigs.getByName("release")
            // Pienennä koodia ProGuardilla
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ""
            versionNameSuffix = ""
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM — hallitsee Compose-kirjastojen versiot automaattisesti
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt (riippuvuusinjektio)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // OpenStreetMap — kartat ilman API-avainta (lisätään viikolla 3)
    implementation(libs.osmdroid.android)

    // CameraX (lisätään viikolla 4)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.camera.extensions)

    // Coil — kuvan lataus (lisätään viikolla 4)
    implementation(libs.coil.compose)

    // ML Kit — kasvintunnistus (lisätään viikolla 5)
    implementation(libs.mlkit.image.labeling)

    // Splash Screen (lisätään viikolla 7)
    implementation(libs.androidx.core.splashscreen)

    // Accompanist Permissions — ajonaikaiset luvat (lisätään viikolla 2–3)
    implementation(libs.accompanist.permissions)

    debugImplementation(libs.androidx.compose.ui.tooling)

    // Firebase BOM – hallitsee versiot automaattisesti
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)       // Kirjautuminen
    implementation(libs.firebase.firestore)  // NoSQL-tietokanta
    implementation(libs.firebase.storage)    // Tiedostojen tallennus

    // Guava ja ListenableFuture CameraX:lle
    implementation(libs.google.guava)
    implementation("androidx.concurrent:concurrent-futures-ktx:1.2.0")
    implementation(libs.androidx.concurrent.futures)

    // Health Connect
    implementation("androidx.health.connect:connect-client:1.1.0-alpha11")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}