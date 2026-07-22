package com.example.fishingstop.feature.inspect.data

import com.example.fishingstop.core.util.IoDispatcher
import com.example.fishingstop.feature.inspect.data.mapper.toRiskAnalysis
import com.example.fishingstop.feature.inspect.data.remote.UrlVisitAnalysisDataSource
import com.example.fishingstop.feature.inspect.domain.model.RiskAnalysis
import com.example.fishingstop.feature.inspect.domain.repository.UrlVisitAnalysisRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UrlVisitAnalysisRepositoryImpl @Inject constructor(
    private val remote: UrlVisitAnalysisDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UrlVisitAnalysisRepository {

    override suspend fun analyzeVisit(url: String): RiskAnalysis =
        withContext(ioDispatcher) { remote.analyzeVisit(url).toRiskAnalysis() }
}
