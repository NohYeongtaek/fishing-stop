package com.rocketdan24.fishingstop.feature.report.domain

import com.rocketdan24.fishingstop.feature.report.domain.model.Report

/**
 * 신고 저장소 인터페이스.
 * 실제 저장 위치(Firestore)는 data 계층이 담당한다.
 */
interface ReportRepository {

    /**
     * 신고를 서버에 전송한다.
     * @return 접수된 신고 번호
     */
    suspend fun submit(report: Report): String
}
