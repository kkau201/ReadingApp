package com.example.dependencies

object Libs {
    object Hilt {
        private const val version = "2.60.1" // https://github.com/google/dagger/releases
        const val GradlePlugin = "com.google.dagger:hilt-android-gradle-plugin:$version"
        const val Android = "com.google.dagger:hilt-android:$version"
        const val KaptCompiler = "com.google.dagger:hilt-android-compiler:$version"
        const val Compiler = "com.google.dagger:hilt-compiler:$version"
        const val Navigation = "androidx.hilt:hilt-navigation-compose:1.4.0"
        const val Testing = "com.google.dagger:hilt-android-testing:$version"
    }

    object Room {
        private const val version = "2.8.5"
        const val Runtime = "androidx.room:room-runtime:$version"
        const val Compiler = "androidx.room:room-compiler:$version"
        const val KspCompiler = "androidx.room:room-compiler:$version"
        const val Ktx = "androidx.room:room-ktx:$version"
    }

    object Destinations {
        private const val version = "2.3.0"
        const val Core = "io.github.raamcosta.compose-destinations:core:$version"
        const val Ksp = "io.github.raamcosta.compose-destinations:ksp:$version"
    }

    object Lifecycle {
        private const val version = "2.11.0"
        const val ViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
        const val Runtime = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
        const val RuntimeCompose = "androidx.lifecycle:lifecycle-runtime-compose:$version"
    }

    object Firebase {
        const val BoM = "com.google.firebase:firebase-bom:35.2.0"
        const val Auth = "com.google.firebase:firebase-auth"
        const val Firestore = "com.google.firebase:firebase-firestore"
    }

    object Credentials {
        private const val version = "1.6.0"
        const val Core = "androidx.credentials:credentials:$version"
        const val PlayServicesAuth = "androidx.credentials:credentials-play-services-auth:$version"
        const val GoogleId = "com.google.android.libraries.identity.googleid:googleid:1.2.1"
    }

    object Misc {
        const val Coil = "io.coil-kt:coil-compose:2.7.0"
        const val Retrofit = "com.squareup.retrofit2:retrofit:3.0.0"
        const val Http = "com.squareup.okhttp3:okhttp:5.5.1"
        const val JsonConverter = "com.squareup.retrofit2:converter-gson:3.0.0"
        const val MaterialIconsExtended = "androidx.compose.material:material-icons-extended"
        const val Lottie = "com.airbnb.android:lottie-compose:6.7.1"
    }

}