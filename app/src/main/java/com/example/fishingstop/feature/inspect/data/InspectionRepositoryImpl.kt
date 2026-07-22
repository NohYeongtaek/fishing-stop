package com.example.fishingstop.feature.inspect.data

import com.example.fishingstop.core.database.dao.InspectionDao
import com.example.fishingstop.core.database.entity.InspectionEntity
import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.core.util.IoDispatcher
import com.example.fishingstop.feature.inspect.data.mapper.toDomain
import com.example.fishingstop.feature.inspect.data.mapper.toRiskAnalysis
import com.example.fishingstop.feature.inspect.data.remote.GeminiAnalysisDataSource
import com.example.fishingstop.feature.inspect.domain.repository.InspectionRepository
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
 *
 * 동일 문자 반복 검사 시 Gemini 재호출을 막기 위해 세션 인메모리 LRU 캐시를 둔다.
 * (@Singleton 이라 앱 실행 동안 유지되고, 재시작하면 비워져 모델 개선 시 stale 결과가 남지 않는다.
 *  검사 기록 저장은 캐시와 무관하게 매번 수행하므로, 같은 문자를 다시 검사해도 기록은 새로 남는다.)
 */
class InspectionRepositoryImpl @Inject constructor(
    private val remote: GeminiAnalysisDataSource,
    private val dao: InspectionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : InspectionRepository {

    // accessOrder=true + removeEldestEntry 로 LRU 동작. 키는 정규화한 원문(해시 충돌로 인한
    // 오판 방지를 위해 문자열 자체를 키로 쓴다). 네트워크 호출은 잠금 밖에서 수행한다.
    private val analysisCache = object : LinkedHashMap<String, RiskAnalysis>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, RiskAnalysis>?): Boolean =
            size > CACHE_MAX
    }

    override suspend fun analyze(text: String): RiskAnalysis =
        withContext(ioDispatcher) { analyzeCached(text) }

    override suspend fun save(text: String, method: InspectMethod, analysis: RiskAnalysis): Long =
        withContext(ioDispatcher) { persist(text, method, analysis) }

    /** 캐시 조회 → 없으면 Gemini 분석 후 저장. 실패(예외)는 캐시하지 않는다(다음에 재시도 가능). */
    private suspend fun analyzeCached(text: String): RiskAnalysis {
        val key = text.trim()
        synchronized(analysisCache) { analysisCache[key] }?.let { return it }
        val result = remote.analyze(text).toRiskAnalysis()
        synchronized(analysisCache) { analysisCache[key] = result }
        return result
    }

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

    companion object {
        /** 세션 분석 캐시 최대 항목 수(넘으면 가장 오래 안 쓴 항목부터 제거). */
        private const val CACHE_MAX = 50
    }
}
