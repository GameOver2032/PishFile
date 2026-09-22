plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// امضای نسخه‌ی انتشار: اگر کلید و رمزها در ~/.gradle/gradle.properties (یا Secretهای CI)
// تعریف شده باشند، نسخه‌ی release به‌صورت خودکار امضا می‌شود؛ در غیر این صورت بدون امضا می‌ماند.
val releaseStoreFilePath: String? =
    (project.findProperty("PISHFILE_STORE_FILE") as String?)?.takeIf { it.isNotBlank() }

android {
    namespace = "ir.pishfile.app"
    compileSdk = 35

    signingConfigs {
        if (releaseStoreFilePath != null) {
            create("release") {
                storeFile = file(releaseStoreFilePath)
                storePassword = project.findProperty("PISHFILE_STORE_PASSWORD") as String
                keyAlias = project.findProperty("PISHFILE_KEY_ALIAS") as String
                keyPassword = project.findProperty("PISHFILE_KEY_PASSWORD") as String
            }
        }
    }

    defaultConfig {
        applicationId = "ir.pishfile.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        resourceConfigurations += listOf("fa", "en")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // اگر کلید انتشار تعریف شده باشد، امضا می‌شود (محلی یا در GitHub Actions)
            if (releaseStoreFilePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

ksp {
    // محل ذخیره‌ی schema دیتابیس — برای مهاجرت‌های آینده و مستندسازی
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Room — دیتابیس محلی (آفلاین)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore — تنظیمات برنامه
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    debugImplementation(libs.androidx.ui.tooling)
}
