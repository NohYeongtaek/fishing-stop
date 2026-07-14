package com.example.fishingstop.feature.report.domain

import com.example.fishingstop.core.util.Anonymizer
import com.example.fishingstop.feature.inspect.domain.InspectionRepository
import com.example.fishingstop.feature.report.domain.model.Report
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

/**
 * 검사 결과를 익명 신고하는 유스케이스.
 *
 * 1) 검사 기록 로드 → 2) 원문을 "제외"하고 signals 만 마스킹해 신고 데이터 구성
 * → 3) 서버 전송 → 4) 성공 시 로컬 기록에 신고 완료 표시(재신고 방지).
 *
 * @return 성공 시 접수된 신고 번호
 */
class SubmitReportUseCase @Inject constructor(
    private val inspectionRepository: InspectionRepository,
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(inspectionId: Long): Result<String> = runCatching {
        val inspection = inspectionRepository.getResult(inspectionId)
            ?: error("신고할 검사 결과를 찾을 수 없습니다.")
        check(!inspection.isReported) { "이미 신고된 검사 결과입니다." }

        val report = Report(
            reportNumber = generateReportNumber(),
            riskLevel = inspection.riskLevel,
            riskScore = inspection.riskScore,
            method = inspection.method,
            // 원문(inputText)은 전송하지 않는다. signals 도 개인정보 마스킹 후 전송.
            signals = inspection.signals.map { Anonymizer.mask(it) },
            createdAt = System.currentTimeMillis()
        )
        val number = reportRepository.submit(report)

        // 전송 성공 후에만 신고 완료로 표시한다.
        inspectionRepository.markReported(inspectionId)
        number
    }

    /** 예: FS-20260714-A1B2C9 형태의 사람이 읽기 쉬운 신고 번호. */
    private fun generateReportNumber(): String {
        val date = SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(Date())
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // 혼동되는 0/O/1/I 제외
        val suffix = (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        return "FS-$date-$suffix"
    }
}
