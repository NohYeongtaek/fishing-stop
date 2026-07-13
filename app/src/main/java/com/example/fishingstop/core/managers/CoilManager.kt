package com.example.fishingstop.core.managers

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import coil3.ImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder

import coil3.request.crossfade

object CoilManager {
    fun create(context: Context): ImageLoader{
        return ImageLoader.Builder(context)
            .crossfade(true) // 부드러운 전환 효과
            .components {
                if (SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
}