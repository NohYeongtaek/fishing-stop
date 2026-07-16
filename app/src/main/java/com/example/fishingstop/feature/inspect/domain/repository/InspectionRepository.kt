package com.example.fishingstop.feature.inspect.domain.repository

import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.feature.inspect.domain.model.InspectionResult
import com.example.fishingstop.feature.inspect.domain.model.RiskAnalysis
import kotlinx.coroutines.flow.Flow

/**
 * 검사(분석 + 저장 + 조회)를 담당하는 저장소 인터페이스.
 *
 * 도메인은 "Gemini로 분석하는지", "Room에 저장하는지"를 알 필요가 없다.
 * 구현(InspectionRepositoryImpl)이 원격 분석과 로컬 저장을 조합한다.
 */
interface InspectionRepository {

    /**
     * 텍스트를 AI로 분석하고 결과를 로컬에 저장한다.
     * @return 저장된 검사 기록의 id (결과 화면으로 전달)
     */
    suspend fun analyzeAndSave(text: String, method: InspectMethod): Long

    /**
     * 텍스트를 AI로 분석만 한다(저장 없음).
     * URL 검사처럼 휴리스틱 결과와 "병합"한 뒤 저장해야 하는 경로에서 사용한다.
     */
    suspend fun analyze(text: String): RiskAnalysis

    /**
     * 이미 계산된 분석 결과(예: URL 휴리스틱)를 로컬에 저장한다.
     * AI를 거치지 않는 검사 경로가 결과 화면으로 합류할 때 사용한다.
     * @return 저장된 검사 기록의 id
     */
    suspend fun save(text: String, method: InspectMethod, analysis: RiskAnalysis): Long

    /** 단건 결과 조회(결과/상세 화면) */
    suspend fun getResult(id: Long): InspectionResult?

    /** 단건 결과 관찰 — 즐겨찾기/신고 상태 변경이 결과 화면에 즉시 반영되도록 */
    fun observeResult(id: Long): Flow<InspectionResult?>

    /** 신고 완료 표시(재신고 방지) */
    suspend fun markReported(id: Long)

    /** 검사 이력(최신순) 관찰 */
    fun observeHistory(): Flow<List<InspectionResult>>

    /** 즐겨찾기한 검사 이력(최신순) 관찰 */
    fun observeFavorites(): Flow<List<InspectionResult>>

    /** 즐겨찾기 토글 */
    suspend fun setFavorite(id: Long, favorite: Boolean)

    /** 검사 기록 삭제 */
    suspend fun delete(id: Long)
}
