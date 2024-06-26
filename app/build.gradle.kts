import com.android.build.api.dsl.BuildFeatures

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.emprende360"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.emprende360"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
    buildFeatures{
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.monitor)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("androidx.core:core-splashscreen:1.2.0-alpha01")
    implementation ("com.google.android.material:material:1.5.0")



    implementation("com.google.android.gms:play-services-auth:21.1.1")

    //QR
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("com.google.zxing:core:3.4.1")


    //Perfil foto
    implementation ("com.google.android.material:material:1.3.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")


    //sql
    implementation ("androidx.sqlite:sqlite:2.1.0")

    //CurvedBottomNavigation
    implementation ("com.github.qamarelsafadi:CurvedBottomNavigation:0.1.3")
    implementation ("com.intuit.sdp:sdp-android:1.1.1")


    //carview2

    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("com.makeramen:roundedimageview:2.3.0")


    implementation ("com.makeramen:roundedimageview:2.3.0")
}