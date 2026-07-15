package com.example.fishingstop.feature.report.data

import com.example.fishingstop.core.util.IoDispatcher
import com.example.fishingstop.feature.report.domain.ReportRepository
import com.example.fishingstop.feature.report.domain.model.Report
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 신고를 Firestore "reports" 컬렉션에 저장하는 구현체.
 *
 * - 저장 문서에는 위험도 메타데이터(등급·점수·방법·마스킹된 근거)만 담는다.
 *   메시지 원문과 사용자 식별 정보는 절대 넣지 않는다.
 * - Firestore Task(콜백)를 코루틴으로 감싼다(추가 의존성 없이).
 */
class ReportRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ReportRepository {

    override suspend fun submit(report: Report): String = withContext(ioDispatcher) {
        // ⚠️ 원문(inputText)은 어떤 형태로도 전송하지 않는다(기획 확정 사항).
        val data = mapOf(
            "reportNumber" to report.reportNumber,
            "riskLevel" to report.riskLevel.name,
            "riskScore" to report.riskScore,
            "method" to report.method.name,
            "signals" to report.signals,
            "createdAt" to report.createdAt
        )

        // Firestore는 오프라인 지속성 때문에 쓰기가 서버에 확인될 때까지 Task가 대기한다.
        // 권한/네트워크 문제로 무한 대기하지 않도록 타임아웃을 둔다.
        withTimeoutOrNull(TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                firestore.collection(COLLECTION)
                    .document(report.reportNumber)
                    .set(data)
                    .addOnSuccessListener { cont.resume(report.reportNumber) }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
        } ?: throw IllegalStateException(
            "신고 전송이 지연되고 있습니다. 네트워크 상태를 확인한 뒤 다시 시도해 주세요."
        )
    }

    companion object {
        private const val COLLECTION = "reports"
        private const val TIMEOUT_MS = 10_000L
    }
}
