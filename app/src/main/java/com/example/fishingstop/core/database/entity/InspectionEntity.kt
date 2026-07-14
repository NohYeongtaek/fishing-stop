package com.example.fishingstop.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 검사 1건의 로컬 저장 단위(검사 기록).
 *
 * 이력 조회/상세보기/즐겨찾기에 사용된다. 로컬(Room)에만 저장되며 서버로 자동 전송되지 않는다.
 * 신고 시에는 별도로 개인정보를 익명화해 서버(Firestore)로 보낸다.
 *
 * @property createdAt  검사 시각(epoch millis). minSdk 24 호환을 위해 java.time 대신 Long 저장.
 * @property method     입력 방식(InspectMethod.name 문자열)
 * @property riskScore  AI 참고 점수(0~100). UI에선 등급 위주로 표시.
 * @property riskLevel  위험도 등급(RiskLevel.name 문자열)
 * @property signals    위험 판단 근거 목록(TypeConverter로 JSON 문자열 저장)
 * @property isFavorite 즐겨찾기 여부
 * @property isReported 신고 완료 여부(재신고 방지용 — 신고 후 결과 화면의 신고 버튼 비활성)
 */
@Entity(tableName = "inspection")
data class InspectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val createdAt: Long,
    val method: String,
    val inputText: String,
    val riskScore: Int,
    val riskLevel: String,
    val advice: String,
    val signals: List<String>,
    val isFavorite: Boolean = false,
    val isReported: Boolean = false
)
