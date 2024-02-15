import com.example.dependencies.Libs

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
    id ("com.google.dagger.hilt.android")
    id ("com.google.devtools.ksp")
}

android {
    namespace = "com.example.readingapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.readingapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.9"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //Hilt Dagger
    implementation(Libs.Hilt.Android)
    kapt(Libs.Hilt.KaptCompiler)
    implementation(Libs.Hilt.Navigation)

    //Room
    implementation(Libs.Room.Runtime)
    annotationProcessor(Libs.Room.Compiler)
    ksp(Libs.Room.KspCompiler)
    implementation(Libs.Room.Ktx)

    //Compose Destination
    implementation(Libs.Destinations.Core)
    ksp(Libs.Destinations.Ksp)

    //lifecycle
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

    // Material Icons
    implementation(Libs.Misc.MaterialIcons)

    // Lottie
    implementation (Libs.Misc.Lottie)

}