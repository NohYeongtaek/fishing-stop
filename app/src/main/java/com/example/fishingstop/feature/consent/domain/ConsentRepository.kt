package com.example.fishingstop.feature.consent.domain

import kotlinx.coroutines.flow.Flow

/**
 * 최초 실행 동의 상태를 다루는 저장소 인터페이스(도메인 계층).
 *
 * 도메인은 "어디에 어떻게 저장하는지(DataStore/DB/서버)"를 몰라야 한다.
 * 실제 구현(DataStore)은 data 계층의 ConsentRepositoryImpl 이 담당한다.
 */
interface ConsentRepository {

    /** 사용자가 AI 분석/개인정보 처리에 동의했는지 여부(변경 시 자동 방출). */
    val hasAgreed: Flow<Boolean>

    /**
     * 동의 화면을 한 번이라도 봤는지 여부.
     * "최초 1회 노출" 요구사항용 — 둘러보기를 선택한(미동의) 사용자가
     * 앱을 다시 켤 때마다 동의 화면을 강제로 보지 않게 한다.
     */
    val hasSeen: Flow<Boolean>

    /** 동의 여부를 저장한다. */
    suspend fun setAgreed(agreed: Boolean)

    /** 동의 화면을 노출했음을 기록한다. */
    suspend fun markSeen()
}
