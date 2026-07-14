package com.newBie.new_bie.core.managers

import com.newBie.new_bie.App
import com.newBie.new_bie.core.utils.API
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitManager {
    val context = App.instance

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .build()
    var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(API.SUPABASE_BASE_URL)
        .client(okHttpClient) // 👈 이거 추가
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}