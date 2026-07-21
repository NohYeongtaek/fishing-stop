plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")

    // @Serializable Routes / DTO 직렬화를 위한 kotlin serialization 플러그인
    alias(libs.plugins.kotlin.serialization)
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
        // BuildConfig.DEBUG로 디버그/릴리스 App Check 프로바이더를 분기하기 위해 필요
        // (AGP 8+부터 기본 비활성).
        buildConfig = true
    }
}

// Room이 컴파일 시점에 생성하는 DB 스키마(JSON)를 저장할 위치.
// 스키마를 버전 관리(git)하면 마이그레이션 검증/테스트에 활용할 수 있다.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
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
    // Icons.Filled.* 등 머티리얼 아이콘. material3의 전이 의존성에 기대지 않고 명시 선언(런타임 누락 방지).
    implementation("androidx.compose.material:material-icons-core")
    // QrCode/Link/Image/Sms 등 core에 없는 아이콘(직접검사 4종)에 필요.
    implementation("androidx.compose.material:material-icons-extended")
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
    // App Check: 디버그 빌드는 Debug 프로바이더, 릴리스 빌드는 Play Integrity 프로바이더를 쓴다
    // (MainActivity에서 BuildConfig.DEBUG로 분기). 디버그 프로바이더를 릴리스에 그대로 쓰면
    // 실질적인 앱 무결성 검증 없이 API가 열려 있어 쿼터/키 도용에 노출된다.
    implementation("com.google.firebase:firebase-ai")
    implementation("com.google.firebase:firebase-appcheck-debug")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")

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

    // ─────────────────────────────────────────────────────────────
    // 피싱멈춰! 추가 의존성 (Phase 1 초기 세팅)
    // ⚠️ 아래 버전들은 AGP 9.1.1 / Kotlin 2.3.10 / compileSdk 37 환경 기준
    //    권장 좌표이며, 최신 호환 버전은 Gradle Sync로 확정 필요.
    // ─────────────────────────────────────────────────────────────

    // Room: 로컬 DB (검사 기록 저장 / 오프라인 우선 저장 후 동기화)
    val roomVersion = "2.8.2"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")          // Coroutine/Flow 지원
    ksp("androidx.room:room-compiler:$roomVersion")                // 어노테이션 처리(KSP)

    // ML Kit: 온디바이스 OCR (캡처 이미지 → 텍스트). 네트워크 전송 없이 기기 내 처리.
    implementation("com.google.mlkit:text-recognition:16.0.1")         // 라틴 문자
    implementation("com.google.mlkit:text-recognition-korean:16.0.1")  // 한글 인식
    // ML Kit: 바코드/QR 스캔 (직접검사 탭의 QR 코드 촬영)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // CameraX: QR 촬영용 카메라 프리뷰/분석 파이프라인
    val cameraxVersion = "1.4.2"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // Firebase: 인증/DB/푸시 (BoM으로 버전 일괄 관리 — 위에서 BoM 이미 추가됨)
    implementation("com.google.firebase:firebase-auth")        // 익명 인증(신고자 식별 최소화)
    implementation("com.google.firebase:firebase-firestore")   // 신고 데이터 저장
    implementation("com.google.firebase:firebase-messaging")   // FCM 푸시

    // Coroutine: Android 메인 디스패처(Dispatchers.Main) 지원
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    // Splash Screen API: 시스템 표준 스플래시(콜드 스타트 로고 표시)
    implementation("androidx.core:core-splashscreen:1.0.1")
}