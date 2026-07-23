package com.rocketdan24.fishingstop.feature.inspect.data.remote.safebrowsing

import com.rocketdan24.fishingstop.feature.inspect.data.remote.safebrowsing.dto.ThreatMatchesRequestDto
import com.rocketdan24.fishingstop.feature.inspect.data.remote.safebrowsing.dto.ThreatMatchesResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/** Google Safe Browsing v4 Lookup API. */
interface SafeBrowsingApi {
    @POST("v4/threatMatches:find")
    suspend fun findThreatMatches(
        @Query("key") apiKey: String,
        @Body request: ThreatMatchesRequestDto
    ): ThreatMatchesResponseDto

    companion object {
        const val BASE_URL = "https://safebrowsing.googleapis.com/"
    }
}
