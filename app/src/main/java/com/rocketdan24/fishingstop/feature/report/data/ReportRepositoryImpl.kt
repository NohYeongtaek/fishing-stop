package com.rocketdan24.fishingstop.feature.report.data

import com.rocketdan24.fishingstop.core.util.IoDispatcher
import com.rocketdan24.fishingstop.feature.report.domain.ReportRepository
import com.rocketdan24.fishingstop.feature.report.domain.model.Report
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
 * 시나리오 B(수동 대신신고): 담당자가 수사기관에 대신 신고할 수 있도록 원문(contentText)과
 * 신고 대상 지표(indicators)를 함께 저장한다. 사용자 식별 정보는 담지 않는다(익명).
 * - Firestore Task(콜백)를 코루틴으로 감싼다(추가 의존성 없이).
 */
class ReportRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ReportRepository {

    override suspend fun submit(report: Report): String = withContext(ioDispatcher) {
        val data = mapOf(
            "reportNumber" to report.reportNumber,
            "riskLevel" to report.riskLevel.name,
            "riskScore" to report.riskScore,
            "method" to report.method.name,
            "signals" to report.signals,
            "contentText" to report.contentText,
            // 지표는 map 리스트로 직렬화
            "indicators" to report.indicators.map {
                mapOf("type" to it.type.name, "value" to it.value, "source" to it.source.name)
            },
            "status" to report.status,
            "handledAt" to report.handledAt,
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
