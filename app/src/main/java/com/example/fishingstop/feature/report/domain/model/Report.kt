package com.example.fishingstop.feature.report.domain.model

import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.core.util.RiskLevel

/**
 * 서버(Firestore)로 전송하는 신고 데이터.
 *
 * 신고 정책(시나리오 B — 수동 대신신고): 담당자가 수집 데이터를 보고 수사기관(counterscam112/1394)에
 * 대신 신고한다. 따라서 문자 원문([contentText])과 신고 대상 지표([indicators])를 함께 보관한다.
 * 사용자 식별 정보는 담지 않는다(익명). 처리 완료 시 담당자가 [handledAt] 을 기록하고 파기한다.
 *
 * @property reportNumber 사용자에게 보여줄 신고 번호(문서 id로도 사용)
 * @property signals      마스킹 처리된 판단 근거 목록(참고용)
 * @property contentText  문자 원문/OCR 텍스트(증거 목적 — 마스킹하지 않음). 링크/QR은 URL.
 * @property indicators   사용자가 확인한 신고 대상 지표(전화번호/링크/계좌 등)
 * @property status       처리 상태("received" 접수됨)
 * @property handledAt    처리 완료 시각(파기 트리거). 미처리면 null.
 */
data class Report(
    val reportNumber: String,
    val riskLevel: RiskLevel,
    val riskScore: Int,
    val method: InspectMethod,
    val signals: List<String>,
    val contentText: String,
    val indicators: List<Indicator>,
    val createdAt: Long,
    val status: String = "received",
    val handledAt: Long? = null
)
