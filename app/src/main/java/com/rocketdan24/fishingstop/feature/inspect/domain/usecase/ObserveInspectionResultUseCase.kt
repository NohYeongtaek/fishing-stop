package com.rocketdan24.fishingstop.feature.inspect.domain.usecase

import com.rocketdan24.fishingstop.feature.inspect.domain.repository.InspectionRepository
import com.rocketdan24.fishingstop.feature.inspect.domain.model.InspectionResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 검사 결과 1건을 관찰하는 유스케이스(결과 화면용).
 * 즐겨찾기/신고 완료 등 상태 변경이 화면에 즉시 반영된다.
 */
class ObserveInspectionResultUseCase @Inject constructor(
    private val repository: InspectionRepository
) {
    operator fun invoke(id: Long): Flow<InspectionResult?> = repository.observeResult(id)
}
