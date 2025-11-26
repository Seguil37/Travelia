plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.proyecto.travelia"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.proyecto.travelia"
        minSdk = 24
        targetSdk = 36
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

    // Java (Room funciona perfecto con 11)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Tus libs existentes
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    // 👇👇 AGREGA ESTAS 3 LÍNEAS PARA QUE FUNCIONE PUBLICAR/LOGIN 👇👇
    implementation("com.google.firebase:firebase-firestore") // Base de datos
    implementation("com.google.firebase:firebase-storage")   // Fotos
    implementation("com.google.firebase:firebase-auth")      // Iniciar Sesión

    // Librerías de Autenticación
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // ===== Favoritos: Room + LiveData (Kotlin DSL) =====
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.4")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
}
