plugins {
    alias(libs.plugins.androidApplication)          // Uporabi vtičnik za Android aplikacijo
    alias(libs.plugins.jetbrainsKotlinAndroid)      // Uporabi vtičnik za Kotlin Android razvoj
}

android {
    namespace = "com.example.myapplication"          // Prostor imen aplikacije
    compileSdk = 34                                  // Različica SDK, s katero se aplikacija prevaja

    defaultConfig {
        applicationId = "com.example.myapplication" // Enolični ID aplikacije
        minSdk = 24                                  // Najnižja podprta različica Android SDK
        targetSdk = 34                               // Ciljna različica Android SDK
        versionCode = 1                              // Interna številka različice aplikacije
        versionName = "1.0"                          // Vidno ime različice aplikacije za uporabnike

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"  // Nastavi testni izvajalnik za instrumentalne teste
        vectorDrawables {
            useSupportLibrary = true                 // Uporabi podporno knjižnico za vektorske risbe na starejših napravah
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false                  // Onemogoči minimizacijo kode (shrinking, obfuscation) za izdajo
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )                                        // Uporabi privzete ProGuard nastavitve in lastne pravila
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11 // Nastavi združljivost izvorne kode z Java 11
        targetCompatibility = JavaVersion.VERSION_11 // Ciljna različica Java bajtne kode je Java 11
    }
    kotlinOptions {
        jvmTarget = "11"                             // Ciljna različica JVM za Kotlin je 11
    }
    buildFeatures {
        compose = true                               // Omogoči Jetpack Compose
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"     // Različica razširitve Kotlin prevajalnika za Compose
    }
    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}") // Izključi določene licence iz končne aplikacije
        }
    }
}

dependencies {
    // Osnovne odvisnosti AndroidX in Kotlin
    implementation(libs.androidx.core.ktx)                     // Jedro AndroidX s Kotlin razširitvami
    implementation(libs.androidx.lifecycle.runtime.ktx)        // Podpora za življenjski cikel z Kotlin razširitvami
    implementation(libs.androidx.activity.compose)             // Podpora za Jetpack Compose v aktivnostih
    implementation(platform(libs.androidx.compose.bom))        // Upravljanje različic za Compose z BOM (Bill of Materials)
    implementation(libs.androidx.ui)                           // Osnovne UI knjižnice za Compose
    implementation(libs.androidx.ui.graphics)                  // Grafične komponente za Compose
    implementation(libs.androidx.ui.tooling.preview)           // Orodja za predogled UI v Compose
    implementation(libs.androidx.material3)                    // Material Design 3 komponente
    implementation(libs.constraintlayout)                      // ConstraintLayout za urejanje postavitve
    implementation(libs.material)                              // Material Design knjižnice

    // Odvisnosti za CameraX (uporaba kamere)
    implementation(libs.androidx.camera.core)                  // Jedro CameraX knjižnice
    implementation(libs.androidx.camera.camera2)               // Podpora za Camera2 API v CameraX
    implementation(libs.androidx.camera.lifecycle)             // Integracija CameraX z življenjskim ciklom aplikacije
    implementation(libs.androidx.camera.view)                  // Pogledi in pripomočki za prikaz kamere
    implementation(libs.androidx.camera.extensions)            // Razširitve za CameraX (npr. portretni način, HDR)

    // Odvisnosti za omrežno komunikacijo z uporabo Retrofit in Gson
    implementation(libs.retrofit)                              // Retrofit za poenostavitev HTTP zahtev
    implementation(libs.retrofit.gson)                         // Gson pretvornik za serializacijo/deserializacijo JSON

    // Opcijsko: OkHttp logging interceptor za odpravljanje težav s HTTP zahtevami
    implementation(libs.okhttp.logging.interceptor)            // Interceptor za logiranje omrežnih zahtev in odgovorov

    // Odvisnosti za testiranje
    testImplementation(libs.junit)                             // JUnit za enotsko testiranje
    androidTestImplementation(libs.androidx.junit)             // JUnit za instrumentalno Android testiranje
    androidTestImplementation(libs.androidx.espresso.core)     // Espresso za UI testiranje na Androidu
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Upravljanje različic za Compose v testih
    androidTestImplementation(libs.androidx.ui.test.junit4)    // Podpora za testiranje Compose z JUnit4
    debugImplementation(libs.androidx.ui.tooling)              // Orodja za razvijalce za Compose (npr. Debug predogled)
    debugImplementation(libs.androidx.ui.test.manifest)        // Manifest za Compose testiranje v načinu za odpravljanje napak
}
