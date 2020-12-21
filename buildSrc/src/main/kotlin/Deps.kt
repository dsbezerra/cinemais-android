object Deps {

    const val coreKtx = "androidx.core:core-ktx:1.3.0-alpha01"

    // UI
    const val appcompat = "androidx.appcompat:appcompat:1.3.0-alpha02"
    const val fragmentKtx = "androidx.fragment:fragment-ktx:1.3.0-alpha08"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.0.0"
    const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01"
    const val material = "com.google.android.material:material:1.3.0-alpha02"

    // Dagger
    object Dagger {
        private const val version = "2.28.3"
        const val dagger = "com.google.dagger:dagger:$version"
    }

    // Glide
    object Glide {
        private const val version = "4.11.0"
        const val glide = "com.github.bumptech.glide:glide:$version"
        const val compiler = "com.github.bumptech.glide:compiler:$version"
        const val transformations = "jp.wasabeef:glide-transformations:4.1.0"
    }

    object Lifecycle {
        private const val version = "2.3.0-alpha07"
        const val livedata = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
        const val viewmodelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
    }

    const val preference = "androidx.preference:preference:1.1.1"

    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlin}"
        const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlin}"
    }

    object Hilt {
        const val version = "1.0.0-alpha02"
        const val viewmodel = "androidx.hilt:hilt-lifecycle-viewmodel:$version"
        const val androidxCompiler = "androidx.hilt:hilt-compiler:$version"
        const val android = "com.google.dagger:hilt-android:${Versions.hilt}"
        const val compiler = "com.google.dagger:hilt-android-compiler:${Versions.hilt}"
    }

    object Firebase {
        const val core = "com.google.firebase:firebase-core:17.5.0"
        const val messaging = "com.google.firebase:firebase-messaging:20.2.4"
    }

    const val jsoup = "org.jsoup:jsoup:1.13.1"

    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:2.4"

    const val notify = "io.karn:notify:1.2.1"

    object Retrofit {
        private const val version = "2.6.0"
        const val retrofit = "com.squareup.retrofit2:retrofit:$version"
        const val mock = "com.squareup.retrofit2:retrofit-mock:$version"
    }

    object SmoothProgressBar {
        const val horizontal = "com.github.castorflex.smoothprogressbar:library:1.1.0"
        const val circular = "com.github.castorflex.smoothprogressbar:library-circular:1.3.0"
    }

    const val timber = "com.jakewharton.timber:timber:4.7.1"

    // Instrumentation tests
    object Test {
        const val coreKtx = "androidx.test:core-ktx:1.3.0-rc03"
        const val runner = "androidx.test:runner:1.2.0"
    }

    // Unit tests
    object UnitTest {
        const val junit = "junit:junit:4.12"
        const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.0.0-RC3"
        const val mockitoCore = "org.mockito:mockito-core:2.23.0"
    }

}