package com.example.fishingstop.feature.report.domain

import com.example.fishingstop.core.util.Anonymizer
import com.example.fishingstop.feature.inspect.domain.InspectionRepository
import com.example.fishingstop.feature.report.domain.model.Indicator
import com.example.fishingstop.feature.report.domain.model.IndicatorSource
import com.example.fishingstop.feature.report.domain.model.IndicatorType
import com.example.fishingstop.feature.report.domain.model.Report
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

/**
 * 검사 결과를 신고하는 유스케이스(시나리오 B — 수동 대신신고).
 *
 * 1) 검사 기록 로드 → 2) 원문(contentText)과 확정 지표를 담아 신고 데이터 구성
 * → 3) 서버 전송 → 4) 성공 시 로컬 기록에 신고 완료 표시(재신고 방지).
 *
 * @param inspectionId       신고 대상 검사 기록 id
 * @param confirmedIndicators 사용자가 화면에서 신고 포함으로 선택한 지표들
 * @param manualPhone        사용자가 직접 입력한 발신 전화번호(없으면 null)
 * @return 성공 시 접수된 신고 번호
 */
class SubmitReportUseCase @Inject constructor(
    private val inspectionRepository: InspectionRepository,
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(
        inspectionId: Long,
        confirmedIndicators: List<Indicator>,
        manualPhone: String?
    ): Result<String> = runCatching {
        val inspection = inspectionRepository.getResult(inspectionId)
            ?: error("신고할 검사 결과를 찾을 수 없습니다.")
        check(!inspection.isReported) { "이미 신고된 검사 결과입니다." }

        // 수동 입력 발신번호가 있으면 지표에 추가한다.
        val indicators = buildList {
            addAll(confirmedIndicators)
            manualPhone?.trim()?.takeIf { it.isNotEmpty() }?.let {
                add(Indicator(IndicatorType.PHONE, it, IndicatorSource.MANUAL))
            }
        }.distinctBy { it.type to it.value }

        val report = Report(
            reportNumber = generateReportNumber(),
            riskLevel = inspection.riskLevel,
            riskScore = inspection.riskScore,
            method = inspection.method,
            // signals는 참고용이라 개인정보 마스킹 유지. 원문(contentText)은 증거 목적이라 raw.
            signals = inspection.signals.map { Anonymizer.mask(it) },
            contentText = inspection.inputText,
            indicators = indicators,
            createdAt = System.currentTimeMillis()
        )
        val number = reportRepository.submit(report)

        // 전송 성공 후에만 신고 완료로 표시한다.
        inspectionRepository.markReported(inspectionId)
        number
    }

    /** 예: FS-20260715-A1B2C9 형태의 사람이 읽기 쉬운 신고 번호. */
    private fun generateReportNumber(): String {
        val date = SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(Date())
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // 혼동되는 0/O/1/I 제외
        val suffix = (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        return "FS-$date-$suffix"
    }
}
