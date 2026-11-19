import com.kaylakautai.inkle.dependencies.Libs
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.kaylakautai.inkle"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.kaylakautai.inkle"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "BOOKS_API_KEY", "\"${booksApiKey()}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        // Lets local unit tests call android.util.Log etc. without crashing.
        unitTests.isReturnDefaultValues = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

fun booksApiKey(): String {
    val secretsFile = rootProject.file("secrets.properties")
    if (!secretsFile.exists()) return ""
    return Properties().apply {
        secretsFile.inputStream().use { load(it) }
    }.getProperty("BOOKS_API_KEY").orEmpty()
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
}

ksp {
    arg("compose-destinations.codeGenPackageName", "com.kaylakautai.inkle.ui")
}

dependencies {

    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    testImplementation(Libs.Testing.CoroutinesTest)
    testImplementation(Libs.Testing.Mockk)
    testImplementation(Libs.Testing.MockWebServer)
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Hilt
    implementation(Libs.Hilt.Android)
    ksp(Libs.Hilt.Compiler)
    implementation(Libs.Hilt.Navigation)

    // Compose Destinations
    implementation(Libs.Destinations.Core)
    ksp(Libs.Destinations.Ksp)

    // Lifecycle
    implementation(Libs.Lifecycle.ViewModel)
    implementation(Libs.Lifecycle.Runtime)
    implementation(Libs.Lifecycle.RuntimeCompose)

    // Coil
    implementation(Libs.Misc.Coil)

    // Retrofit
    implementation(Libs.Misc.Retrofit)

    // OkHttp
    implementation(Libs.Misc.Http)

    // JSON Converter
    implementation(Libs.Misc.JsonConverter)

    // Material
    implementation(Libs.Misc.MaterialIconsExtended)

    // Lottie
    implementation(Libs.Misc.Lottie)

    // Firebase
    implementation(platform(Libs.Firebase.BoM))
    implementation(Libs.Firebase.Auth)
    implementation(Libs.Firebase.Firestore)

    // Credential Manager (Sign in with Google)
    implementation(Libs.Credentials.Core)
    implementation(Libs.Credentials.PlayServicesAuth)
    implementation(Libs.Credentials.GoogleId)

}
