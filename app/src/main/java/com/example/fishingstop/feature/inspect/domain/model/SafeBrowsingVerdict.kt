package com.example.fishingstop.feature.inspect.domain.model

/**
 * Google Safe Browsing v4 조회 결과.
 *
 * 1차 검사(즉시·무비용에 가까움)의 판정. [Threat]면 이미 알려진 악성/피싱 사이트이므로
 * 2차(AI 방문 분석)를 생략하고 바로 위험 확정에 사용한다.
 */
sealed interface SafeBrowsingVerdict {
    /** 위협 데이터베이스에 매치됨. @property threatTypes 매치된 위협 유형(예: SOCIAL_ENGINEERING, MALWARE) */
    data class Threat(val threatTypes: List<String>) : SafeBrowsingVerdict

    /** 매치 없음 — 알려진 위협 목록엔 없다는 뜻일 뿐, 안전을 보증하지는 않는다. */
    data object Clean : SafeBrowsingVerdict

    /** API 키 미설정, 네트워크 오류, 타임아웃 등으로 판정할 수 없음. */
    data object Unknown : SafeBrowsingVerdict
}
