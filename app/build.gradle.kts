plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.fishingstop"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.fishingstop"
        minSdk = 24
        targetSdk = 37
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    //noinspection WrongGradleMethod
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Firebase 플랫폼의 BoM(Bill of Materials)을 추가합니다.
    // → Firebase 라이브러리들의 버전을 한 번에 관리해 줍니다.
    implementation(platform("com.google.firebase:firebase-bom:34.16.0"))

    // Firebase AI 기능을 사용하기 위한 라이브러리를 추가합니다.
    // App Check(디버그용) 라이브러리를 함께 추가합니다.
    implementation("com.google.firebase:firebase-ai")
    implementation("com.google.firebase:firebase-appcheck-debug")

    // BoM을 사용하므로 Firebase 라이브러리에는
    // 버전을 따로 작성하지 않아도 됩니다.
    //레트로핏
    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    // 서버에서 들어오는 데이터의 공통 규칙
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    // 컨버터 Json을 Kotlin으로 바꿔주는 컨버터는 모두 바꾸는 것
    // 서버에서 들어온 Json 데이터를 안드에서 사용하는 dataClass로 바꿔주는 것
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    // 통신 라이브러리
    implementation("com.squareup.okhttp3:okhttp:5.4.0")// inter = between
    // cept - catch
    implementation("com.squareup.okhttp3:logging-interceptor:5.4.0")

    // 뷰모델
    val lifecycle_version = "2.11.0"
    // viewModelScope를 사용할 수 있게 만듦
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${lifecycle_version}")
    // @Composable를 사용할 수 있게 만듦
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${lifecycle_version}")
    // launch, async, Dispatchers.IO, Dispatchers.Main 사용할 수 있게 만듦
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

    // dataStore 저장소 파일에 접근하는 통로(인스턴스 객체)를 만드는 코드를 앱 전체에서 딱 한 번만 작성해서 사용하는 라이브러리(ex: 코치마크, 라이트다크모드, 큰 글씨 모드)
    implementation("androidx.datastore:datastore-preferences:1.2.1")

    //navigation
    val nav_version = "2.9.3"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // 인터넷 이미지 띄우기
    implementation("io.coil-kt.coil3:coil-compose:3.5.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.5.0")
    // Coil gif
    implementation("io.coil-kt.coil3:coil-gif:3.5.0")

    // Zoomable
    val ZoomableVersion = "2.13.0"
    implementation("net.engawapg.lib:zoomable:${ZoomableVersion}")
    // dotsindicator
    implementation("com.tbuonomo:dotsindicator:5.1.1")

    implementation("com.google.dagger:hilt-android:2.60.1")
    ksp("com.google.dagger:hilt-android-compiler:2.60.1")
    // Compose에서 hiltViewModel() 함수를 사용하기 위한 라이브러리
    implementation("androidx.hilt:hilt-navigation-compose:1.4.0")

    // QR 코드 촬영 화면 - 카메라 미리보기/프레임 분석
    val cameraXVersion = "1.4.1"
    implementation("androidx.camera:camera-core:${cameraXVersion}")
    implementation("androidx.camera:camera-camera2:${cameraXVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraXVersion}")
    implementation("androidx.camera:camera-view:${cameraXVersion}")
    // QR/바코드 온디바이스 인식
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
}