import java.util.Properties
import java.security.MessageDigest

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("local.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    namespace = "com.ryanshelby.iptv"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ryanshelby.iptv"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "2.0.0"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties.getProperty("RELEASE_STORE_FILE") ?: "../release.keystore")
            storePassword = keystoreProperties.getProperty("RELEASE_STORE_PASSWORD") ?: ""
            keyAlias = keystoreProperties.getProperty("RELEASE_KEY_ALIAS") ?: "release"
            keyPassword = keystoreProperties.getProperty("RELEASE_KEY_PASSWORD") ?: ""
            enableV4Signing = true
        }
        getByName("debug") {
            enableV4Signing = true
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.11" }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.04.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Media3 ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.6.0")
}

tasks.whenTaskAdded {
    val taskName = name
    if (taskName == "assembleRelease" || taskName == "assembleDebug") {
        doLast {
            val variant = if (taskName == "assembleRelease") "release" else "debug"
            val apkDir = File(layout.buildDirectory.get().asFile, "outputs/apk/$variant")
            if (apkDir.exists()) {
                apkDir.listFiles()?.filter { it.name.endsWith(".apk") }?.forEach { apkFile ->
                    val digest = MessageDigest.getInstance("SHA-256")
                    val buffer = ByteArray(8192)
                    apkFile.inputStream().use { fis ->
                        var bytesRead: Int
                        while (fis.read(buffer).also { bytesRead = it } != -1) {
                            digest.update(buffer, 0, bytesRead)
                        }
                    }
                    val hash = digest.digest().joinToString("") { "%02x".format(it) }
                    val sha256File = File("${apkFile.absolutePath}.sha256")
                    sha256File.writeText("$hash  ${apkFile.name}")
                    println("--> Generated SHA-256 checksum: ${sha256File.name}")
                }
            }
        }
    }
}
