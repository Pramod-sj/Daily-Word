plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt) // Applies Dagger Hilt
    alias(libs.plugins.ksp) // Applies KSP
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.pramod.games.crossword"
    compileSdk {
        version = release(35)
    }

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.coreKtx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidxTestJunit)
    androidTestImplementation(libs.espressoCore)

    // Android lifecycle libraries
    implementation(libs.viewmodelKtx) // ViewModel support with KTX
    implementation(libs.livedataKtx) // LiveData support with KTX
    implementation(libs.lifecycleExtensions) // Extensions for Android lifecycle

    // Jetpack Compose libraries
    implementation(platform(libs.composeBom)) // Jetpack Compose BOM for versioning
    androidTestImplementation(platform(libs.composeBom)) // Compose BOM for UI tests
    implementation(libs.composeUi) // Jetpack Compose UI library
    implementation(libs.composeMaterial) // Jetpack Compose Material library
    implementation(libs.composeMaterial3) // Jetpack Compose Material3 library
    implementation(libs.composeMaterialIconsExtended) // Full set of Material Icons
    implementation(libs.composeUiToolingPreview) // UI tooling preview for Compose
    implementation(libs.activityCompose) // Activity support for Jetpack Compose
    debugImplementation(libs.composeUiTooling) // UI tooling for debugging Compose

    // Dagger Hilt for dependency injection
    implementation(libs.hiltAndroid) // Hilt for Android dependency injection
    ksp(libs.hiltCompiler) // Annotation processor for Hilt
    implementation(libs.hiltNavigationFragment) // Hilt navigation fragment support
    implementation(libs.hiltWork) // Hilt support for WorkManager

    // Accompanist library for additional Compose utilities
    implementation(libs.accompanistThemeAdapter) // Theme adapter for Compose

    implementation(libs.lottieCompose) // Lottie for Jetpack Compose


    implementation(project(":router"))
    implementation(project(":core:network"))
    implementation(project(":core:preferences"))
    implementation(project(":games:common:game-rules"))
    implementation(project(":games:common:game-results"))

    // Logging with OkHttp
    implementation(libs.okhttpLoggingInterceptor) // Logging interceptor for OkHttp
}
