package com.rocketdan24.fishingstop.feature.notice.domain

/**
 * 공지 작성 권한(관리자) 게이트.
 *
 * PIN 검증은 클라이언트 로컬에서 이뤄지며 암호학적 방어가 아니라 "우발적/일반 사용자 진입 차단"
 * 목적이다(자세한 한계는 docs/prompt-notice-admin.md 참고). PIN 해시는 Firestore config/adminGate
 * 문서에서 읽어오므로 앱 재배포 없이 콘솔에서 교체할 수 있다.
 * 5회 연속 실패 시 30분간 기기 로컬 잠금(DataStore에 보관, 재시작해도 유지).
 */
interface AdminGateRepository {

    /** 4자리 PIN을 검증한다. 잠금/실패 횟수/네트워크 오류까지 결과로 표현한다. */
    suspend fun verifyPin(pin: String): PinResult
}

/** PIN 검증 결과. */
sealed interface PinResult {
    /** 통과(작성 화면 진입 가능). */
    data object Success : PinResult

    /** 실패. @param remaining 잠금까지 남은 시도 횟수. */
    data class Failed(val remaining: Int) : PinResult

    /** 잠김. @param remainingMillis 잠금 해제까지 남은 시간(ms). */
    data class Locked(val remainingMillis: Long) : PinResult

    /** 검증 자체 실패(예: PIN 해시 로드 네트워크 오류). */
    data class Error(val message: String) : PinResult
}
