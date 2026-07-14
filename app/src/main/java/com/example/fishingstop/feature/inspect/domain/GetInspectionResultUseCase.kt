package com.example.fishingstop.feature.inspect.domain

import com.example.fishingstop.feature.inspect.domain.model.InspectionResult
import javax.inject.Inject

/** 저장된 검사 결과 1건을 id로 불러오는 유스케이스(결과/상세 화면용). */
class GetInspectionResultUseCase @Inject constructor(
    private val repository: InspectionRepository
) {
    suspend operator fun invoke(id: Long): InspectionResult? = repository.getResult(id)
}
