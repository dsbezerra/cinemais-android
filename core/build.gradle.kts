plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(Versions.compileSdk)
    defaultConfig {
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    // Dagger
    implementation(Deps.Dagger.dagger)

    // Jsoup
    implementation(Deps.jsoup)

    // Retrofit
    implementation(Deps.Retrofit.retrofit)
    testImplementation(Deps.Retrofit.mock)

    // Timber
    implementation(Deps.timber)

    // Local unit tests
    testImplementation(Deps.UnitTest.junit)
    testImplementation(Deps.UnitTest.mockitoCore)
    testImplementation(Deps.UnitTest.mockitoKotlin)
}