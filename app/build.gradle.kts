plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.healthgo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.healthgo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // add bottom nav bar dependencies
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    // daily steps dependencies
    implementation("com.google.android.gms:play-services-auth:20.0.0")
    implementation("com.google.android.gms:play-services-fitness:21.0.1")

    // chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // circular progress bar
    implementation ("com.mikhaellopez:circularprogressbar:3.1.0")
}