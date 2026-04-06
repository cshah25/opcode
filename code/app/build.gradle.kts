import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs")
}

val secretsProperties = Properties()
val secretsFile = rootProject.file("secrets.properties")

if (secretsFile.exists()) {
    secretsProperties.load(FileInputStream(secretsFile))
}

android {
    tasks.withType<Test> {
        useJUnitPlatform()
    }

    namespace = "com.example.opcodeapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.opcodeapp"
        minSdk = 36
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GEOAPIFY_API_KEY",
            "\"${secretsProperties.getProperty("GEOAPIFY_API_KEY", "")}\""
        )

        buildConfigField(
            "String",
            "GEOAPIFY_GEOLOCATION_API_KEY",
            "\"${secretsProperties.getProperty("GEOAPIFY_GEOLOCATION_API_KEY", "")}\""
        )
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

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    // Core UI
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.annotation)

    implementation(libs.google.libphonenumber)

    // Navigation
    implementation(libs.bundles.navigation)
    implementation(libs.glide)

    // Networking / utilities
    implementation(libs.square.okhttp)
    implementation(libs.journeyapps.zxing)
    implementation(libs.opencsv)


    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    // Camera / QR scanning
    implementation(libs.bundles.camerax)
    implementation(libs.google.mlkit.barcode)

    // Unit tests
    testImplementation(libs.junit4)
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)

    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito:mockito-inline:5.2.0")

    // Android tests
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.bundles.espresso)

    debugImplementation(libs.androidx.fragment.testing)
}