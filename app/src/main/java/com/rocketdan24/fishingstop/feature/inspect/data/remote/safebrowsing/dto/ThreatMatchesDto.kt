package com.rocketdan24.fishingstop.feature.inspect.data.remote.safebrowsing.dto

import com.google.gson.annotations.SerializedName

/**
 * Google Safe Browsing v4 threatMatches:find 요청/응답 DTO.
 * 명세: https://developers.google.com/safe-browsing/v4/lookup-api
 */
data class ThreatMatchesRequestDto(
    @SerializedName("client") val client: ClientDto,
    @SerializedName("threatInfo") val threatInfo: ThreatInfoDto
) {
    data class ClientDto(
        @SerializedName("clientId") val clientId: String,
        @SerializedName("clientVersion") val clientVersion: String
    )

    data class ThreatInfoDto(
        @SerializedName("threatTypes") val threatTypes: List<String>,
        @SerializedName("platformTypes") val platformTypes: List<String>,
        @SerializedName("threatEntryTypes") val threatEntryTypes: List<String>,
        @SerializedName("threatEntries") val threatEntries: List<ThreatEntryDto>
    )

    data class ThreatEntryDto(@SerializedName("url") val url: String)

    companion object {
        /** 스미싱/피싱 탐지에 실질적으로 의미 있는 위협 유형만 조회한다. */
        val THREAT_TYPES = listOf(
            "MALWARE",
            "SOCIAL_ENGINEERING",
            "UNWANTED_SOFTWARE",
            "POTENTIALLY_HARMFUL_APPLICATION"
        )

        fun forUrl(url: String, clientId: String, clientVersion: String): ThreatMatchesRequestDto =
            ThreatMatchesRequestDto(
                client = ClientDto(clientId, clientVersion),
                threatInfo = ThreatInfoDto(
                    threatTypes = THREAT_TYPES,
                    platformTypes = listOf("ANY_PLATFORM"),
                    threatEntryTypes = listOf("URL"),
                    threatEntries = listOf(ThreatEntryDto(url))
                )
            )
    }
}

/** 매치가 없으면 Safe Browsing은 빈 JSON 객체(`{}`)를 반환한다 → [matches]는 null. */
data class ThreatMatchesResponseDto(
    @SerializedName("matches") val matches: List<ThreatMatchDto>?
) {
    data class ThreatMatchDto(@SerializedName("threatType") val threatType: String?)
}
