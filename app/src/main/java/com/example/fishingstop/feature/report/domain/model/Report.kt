package com.example.fishingstop.feature.report.domain.model

import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.core.util.RiskLevel

/**
 * 서버(Firestore)로 전송하는 신고 데이터.
 *
 * ⚠️ 개인정보 원칙(기획 확정): 메시지 "원문(inputText)은 절대 전송하지 않는다".
 * 전송 항목은 등급·점수·방법·판단근거(signals)뿐이며, signals 도 혹시 모를
 * 개인정보 잔존에 대비해 마스킹을 거친 값만 담는다. 사용자 식별 정보도 없다(익명).
 *
 * @property reportNumber 사용자에게 보여줄 신고 번호(문서 id로도 사용)
 * @property signals 마스킹 처리된 판단 근거 목록
 */
data class Report(
    val reportNumber: String,
    val riskLevel: RiskLevel,
    val riskScore: Int,
    val method: InspectMethod,
    val signals: List<String>,
    val createdAt: Long
)
