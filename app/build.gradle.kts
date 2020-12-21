plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdkVersion(Versions.compileSdk)
    defaultConfig {
        applicationId = "com.diegobezerra.cinemaisapp"
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode = Versions.versionCode
        versionName = Versions.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        dataBinding = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            versionNameSuffix = Versions.versionSuffix
        }
    }

    lintOptions {
        // Eliminates UnusedResources false positives for resources used in DataBinding layouts
        isCheckGeneratedSources = true
        // Running lint over the debug variant is enough
        isCheckReleaseBuilds = false
        // See lint.xml for rules configuration
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core"))

    implementation(Deps.coreKtx)

    // UI
    implementation(Deps.appcompat)
    implementation(Deps.fragmentKtx)
    implementation(Deps.constraintLayout)
    implementation(Deps.swipeRefreshLayout)
    implementation(Deps.material)
    implementation(Deps.SmoothProgressBar.horizontal)
    implementation(Deps.SmoothProgressBar.circular)

    // Architecture Components
    implementation(Deps.preference)

    // Dagger Hilt
    implementation(Deps.Hilt.android)
    kapt(Deps.Hilt.compiler)
    kapt(Deps.Hilt.androidxCompiler)
    implementation(Deps.Hilt.viewmodel)
    kaptAndroidTest(Deps.Hilt.compiler)
    kaptAndroidTest(Deps.Hilt.androidxCompiler)

    // Glide
    implementation(Deps.Glide.glide)
    implementation(Deps.Glide.transformations)
    kapt(Deps.Glide.compiler)

    // Firebase
    implementation(Deps.Firebase.core)
    implementation(Deps.Firebase.messaging)

    // Kotlin
    implementation(Deps.Kotlin.stdlib)

    // Leak Canary
    debugImplementation(Deps.leakCanary)

    // Notify
    implementation(Deps.notify)

    // Retrofit
    implementation(Deps.Retrofit.retrofit)

    // Timber
    implementation(Deps.timber)

    // Instrumentation tests
    androidTestImplementation(Deps.Test.coreKtx)
    androidTestImplementation(Deps.Test.runner)

    // Local unit tests
    testImplementation(Deps.Retrofit.mock)
    testImplementation(Deps.UnitTest.junit)
    testImplementation(Deps.UnitTest.mockitoCore)
    testImplementation(Deps.UnitTest.mockitoKotlin)
}

repositories {
    mavenCentral()
}

apply(plugin = "com.google.gms.google-services")
