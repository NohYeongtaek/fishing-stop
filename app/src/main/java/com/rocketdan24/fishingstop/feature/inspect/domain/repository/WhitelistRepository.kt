package com.rocketdan24.fishingstop.feature.inspect.domain.repository

/**
 * 안심 도메인(화이트리스트) 저장소.
 * URL 검사에서 이 목록에 있는 도메인은 안전 처리한다.
 */
interface WhitelistRepository {

    /** 기본 내장 공식 도메인 + 사용자/서버 추가분을 합친 안심 도메인 집합. */
    suspend fun getSafeDomains(): Set<String>
}
