package com.rocketdan24.fishingstop.feature.inspect.domain.repository

import com.rocketdan24.fishingstop.feature.inspect.domain.model.SafeBrowsingVerdict

/**
 * Google Safe Browsing v4(threatMatches:find)로 URL 평판을 조회하는 저장소.
 *
 * URL 검사의 1차 관문 — 이미 알려진 악성/피싱 URL인지 즉시 대조한다.
 * 신종(블랙리스트 미등재) URL은 [SafeBrowsingVerdict.Clean]으로 나오므로,
 * 이 결과만으로 "안전"을 확정하면 안 되고 2차(AI 방문 분석)로 이어가야 한다.
 */
interface SafeBrowsingRepository {
    /** @return 조회 결과. API 키 미설정/네트워크 오류 시 [SafeBrowsingVerdict.Unknown](예외를 던지지 않음). */
    suspend fun checkUrl(url: String): SafeBrowsingVerdict
}
