package com.example.dependencies

object Libs {
    object Hilt {
        private const val version = "2.50" // https://mvnrepository.com/artifact/com.google.dagger/hilt-android-gradle-plugin
        const val GradlePlugin = "com.google.dagger:hilt-android-gradle-plugin:$version"
        const val Android = "com.google.dagger:hilt-android:$version"
        const val KaptCompiler = "com.google.dagger:hilt-android-compiler:$version"
        const val Compiler = "com.google.dagger:hilt-compiler:$version"
        const val Navigation = "androidx.hilt:hilt-navigation-compose:1.1.0"
        const val Testing = "com.google.dagger:hilt-android-testing:$version"
    }

    object Room {
        private const val version = "2.6.1"
        const val Runtime = "androidx.room:room-runtime:$version"
        const val Compiler = "androidx.room:room-compiler:$version"
        // To use Kotlin annotation processing tool (kapt)
        const val KspCompiler = "androidx.room:room-compiler:$version"
        // KTX for coroutines
        const val Ktx = "androidx.room:room-ktx:$version"
    }

    object Destinations {
        private const val version = "1.10.0"
        const val Core = "io.github.raamcosta.compose-destinations:core:$version"
        const val Ksp = "io.github.raamcosta.compose-destinations:ksp:$version"
    }

    object Lifecycle {
        private const val version = "2.7.0"
        const val ViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
        const val Runtime = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
        const val RuntimeCompose = "androidx.lifecycle:lifecycle-runtime-compose:$version"
    }

    object Firebase {
        const val BoM = "com.google.firebase:firebase-bom:32.7.2"
        const val Auth = "com.google.firebase:firebase-auth"
        const val Firestore = "com.google.firebase:firebase-firestore"
    }

    object Misc {
        private const val composeVersion = "1.6.1"
        const val Coil = "io.coil-kt:coil-compose:2.4.0"
        const val Retrofit = "com.squareup.retrofit2:retrofit:2.9.0"
        const val Http = "com.squareup.okhttp3:okhttp:5.0.0-alpha.2"
        const val JsonConverter = "com.squareup.retrofit2:converter-gson:2.9.0"
        const val MaterialIcons = "androidx.compose.material:material:$composeVersion"
        const val MaterialIconsExtended = "androidx.compose.material:material-icons-extended:$composeVersion"
        const val Lottie = "com.airbnb.android:lottie-compose:6.3.0"
    }

}