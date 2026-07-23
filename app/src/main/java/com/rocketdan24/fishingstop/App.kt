package com.rocketdan24.fishingstop

import android.app.Application
import android.util.Log
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.rocketdan24.fishingstop.core.fcm.FishingStopFcmService
import com.rocketdan24.fishingstop.core.managers.CoilManager
import com.rocketdan24.fishingstop.core.utils.Constants.TAG
import com.rocketdan24.fishingstop.feature.settings.domain.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), SingletonImageLoader.Factory {
    @Inject
    lateinit var settingsRepository: SettingsRepository

    companion object {
        private var _instance: App? = null
        val instance: App get() = _instance ?: throw IllegalArgumentException("App이 없습니다.")

        // 필요할 때 어디서든 Context를 편하게 쓰기 위한 헬퍼
        val context: PlatformContext get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this

        FishingStopFcmService.ensureNotificationChannel(this)

        // 로그인이 없어 기기별 토큰을 저장할 곳이 없으므로, 전체 공지 토픽 구독 방식을 쓴다.
        // 사용자가 설정에서 알림을 꺼둔 상태라면 재구독하지 않는다.
        CoroutineScope(Dispatchers.Default).launch {
            val enabled = settingsRepository.notificationEnabled.first()
            Log.d(TAG, "onCreate: 저장된 알림 설정 = $enabled")
            if (enabled) {
                FishingStopFcmService.subscribeNotice()
            }
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        // instance 대신 매개변수로 들어온 context를 사용하는 것이 더 권장
        return CoilManager.create(context = context)
    }
}