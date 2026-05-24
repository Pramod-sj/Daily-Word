plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt) // Applies Dagger Hilt
    alias(libs.plugins.ksp) // Applies KSP
}

android {
    namespace = "com.pramod.dailyword.core.shared_preference"
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
                "proguard-rules.pro"
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
}

dependencies {
    implementation(libs.coreKtx)
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidxTestJunit)
    androidTestImplementation(libs.espressoCore)

    // Dagger Hilt for dependency injection
    implementation(libs.hiltAndroid) // Hilt for Android dependency injection
    ksp(libs.hiltCompiler) // Annotation processor for Hilt

    implementation(project(":core:preferences"))
}