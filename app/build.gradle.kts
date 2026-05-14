import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Ensure we use a full JDK with jlink
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

android {
    namespace = "kr.ac.waltdev29.hakplace"
    compileSdk = 36

    defaultConfig {
        applicationId = "kr.ac.waltdev29.hakplace"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val env = Properties()
        val envFile = project.rootProject.file(".env")
        if (envFile.exists()) {
            env.load(envFile.inputStream())
        }
        buildConfigField("String", "API_URL", "\"${env.getProperty("API_URL") ?: ""}\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    
    // Image Loading
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    
    // Utils
    implementation(libs.dotenv)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}