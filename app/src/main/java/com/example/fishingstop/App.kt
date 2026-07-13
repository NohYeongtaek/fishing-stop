package com.example.fishingstop

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.example.fishingstop.core.managers.CoilManager
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
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        // instance 대신 매개변수로 들어온 context를 사용하는 것이 더 권장
        return CoilManager.create(context = context)
    }
}