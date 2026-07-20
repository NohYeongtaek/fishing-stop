package com.example.fishingstop

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.example.fishingstop.core.fcm.FishingStopFcmService
import com.example.fishingstop.core.managers.CoilManager
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(), SingletonImageLoader.Factory {
    companion object {
        private var _instance: App? = null
        val instance: App get() = _instance ?: throw IllegalArgumentException("App이 없습니다.")

        // 필요할 때 어디서든 Context를 편하게 쓰기 위한 헬퍼
        val context: PlatformContext get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this

        // 로그인이 없어 기기별 토큰을 저장할 곳이 없으므로, 전체 공지 토픽 구독 방식을 쓴다.
        FishingStopFcmService.ensureNotificationChannel(this)
        FirebaseMessaging.getInstance().subscribeToTopic(FishingStopFcmService.NOTICE_TOPIC)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        // instance 대신 매개변수로 들어온 context를 사용하는 것이 더 권장
        return CoilManager.create(context = context)
    }
}