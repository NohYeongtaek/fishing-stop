package com.rocketdan24.fishingstop.feature.education.domain

/**
 * 피싱 예방 교육의 사기 유형 카테고리.
 *
 * @property id 라우트 인자로 쓰는 식별자
 * @property title 유형 이름(예: 기관 사칭)
 * @property summary 한 줄 요약
 * @property warningSigns 이런 신호가 보이면 의심하세요(체크리스트)
 * @property tips 대응 방법
 */
data class EducationCategory(
    val id: String,
    val title: String,
    val summary: String,
    val warningSigns: List<String>,
    val tips: List<String>
)
