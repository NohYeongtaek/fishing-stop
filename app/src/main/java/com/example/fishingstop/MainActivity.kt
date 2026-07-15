package com.example.fishingstop

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import dagger.hilt.android.AndroidEntryPoint

/**
 * 단일 액티비티 + Compose Navigation 구조의 진입점.
 *
 * @AndroidEntryPoint 로 Hilt 주입을 활성화한다(@HiltAndroidApp 인 App 과 짝).
 * 메시지 앱에서 "공유(ACTION_SEND)"로 넘어온 텍스트를 받아 분석 플로우로 전달한다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 시스템 스플래시(Splash Screen API) — super.onCreate 이전에 설치해야 한다.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Firebase 초기화 및 App Check(디버그) 설치
        Firebase.initialize(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        // 공유로 전달된 텍스트(text/plain). 런처로 실행되면 null.
        val sharedText = extractSharedText(intent)

        enableEdgeToEdge()
        setContent {
            // 테마/어르신 모드 적용은 FishingStopApp 루트에서 담당한다.
            FishingStopApp(
                sharedText = sharedText,
                // 동의 거부 시 앱 종료(태스크까지 제거해 재실행 시 다시 동의 화면부터).
                onExitApp = { finishAndRemoveTask() }
            )
        }
    }

    /**
     * ACTION_SEND + text/plain 인텐트에서 공유 텍스트를 추출한다.
     * 그 외(런처 실행 등)에는 null 을 반환한다.
     * (이미지 공유는 OCR 단계에서 별도로 처리한다.)
     */
    private fun extractSharedText(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (intent.type != "text/plain") return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() }
    }
}
