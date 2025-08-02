plugins {
    alias(deps.plugins.kmp)
    alias(deps.plugins.compose.compiler)
    alias(deps.plugins.compose)
    alias(deps.plugins.android.application)
    alias(deps.plugins.serialization)
    alias(deps.plugins.compilugin)
}

compilugin {
    enabled.set(true)
    functionsVisitorEnabled.set(true)
    functionsVisitorAnnotation.set("TrackIt")
    functionsVisitorPath.set("dev.supersam.android.app.FunctionsVisitor.visit")
}

kotlin {
    androidTarget()
    sourceSets {

        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material3)

                implementation(deps.serializationJson)
            }
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activity.compose)
        }
    }
}

android {
    namespace = "org.company.app"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        targetSdk = 35

        applicationId = "org.company.app.androidApp"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
