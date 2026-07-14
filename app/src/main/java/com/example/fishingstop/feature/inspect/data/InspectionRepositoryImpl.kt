package com.example.fishingstop.feature.inspect.data

import com.example.fishingstop.core.database.dao.InspectionDao
import com.example.fishingstop.core.database.entity.InspectionEntity
import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.core.util.IoDispatcher
import com.example.fishingstop.feature.inspect.data.mapper.toDomain
import com.example.fishingstop.feature.inspect.data.mapper.toRiskAnalysis
import com.example.fishingstop.feature.inspect.data.remote.GeminiAnalysisDataSource
import com.example.fishingstop.feature.inspect.domain.InspectionRepository
import com.example.fishingstop.feature.inspect.domain.model.InspectionResult
import com.example.fishingstop.feature.inspect.domain.model.RiskAnalysis
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 원격 분석(Gemini) + 로컬 저장(Room)을 조합하는 저장소 구현체.
 * 모든 블로킹 작업은 IO 디스패처에서 수행한다.
 */
class InspectionRepositoryImpl @Inject constructor(
    private val remote: GeminiAnalysisDataSource,
    private val dao: InspectionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : InspectionRepository {

    override suspend fun analyzeAndSave(text: String, method: InspectMethod): Long =
        withContext(ioDispatcher) {
            // 1) Gemini 분석 → 2) 점수 기반 등급 도출 → 3) 로컬 저장
            val analysis = remote.analyze(text).toRiskAnalysis()
            persist(text, method, analysis)
        }

    override suspend fun save(text: String, method: InspectMethod, analysis: RiskAnalysis): Long =
        withContext(ioDispatcher) { persist(text, method, analysis) }

    override suspend fun analyze(text: String): RiskAnalysis =
        withContext(ioDispatcher) { remote.analyze(text).toRiskAnalysis() }

    /** 분석 결과를 검사 기록 엔티티로 만들어 저장한다(AI/휴리스틱 공통). */
    private suspend fun persist(text: String, method: InspectMethod, analysis: RiskAnalysis): Long {
        val entity = InspectionEntity(
            createdAt = System.currentTimeMillis(),
            method = method.name,
            inputText = text,
            riskScore = analysis.riskScore,
            riskLevel = analysis.riskLevel.name,
            advice = analysis.advice,
            signals = analysis.signals
        )
        return dao.insert(entity)
    }

    override suspend fun getResult(id: Long): InspectionResult? =
        withContext(ioDispatcher) { dao.getById(id)?.toDomain() }

    override fun observeResult(id: Long): Flow<InspectionResult?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun markReported(id: Long) =
        withContext(ioDispatcher) { dao.markReported(id) }

    override fun observeHistory(): Flow<List<InspectionResult>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeFavorites(): Flow<List<InspectionResult>> =
        dao.observeFavorites().map { list -> list.map { it.toDomain() } }

    override suspend fun setFavorite(id: Long, favorite: Boolean) =
        withContext(ioDispatcher) { dao.setFavorite(id, favorite) }

    override suspend fun delete(id: Long) =
        withContext(ioDispatcher) { dao.deleteById(id) }
}
