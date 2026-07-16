package com.example.fishingstop.feature.inspect.domain.usecase

import com.example.fishingstop.feature.inspect.domain.repository.InspectionRepository
import javax.inject.Inject

/** 검사 기록 1건을 삭제하는 유스케이스. */
class DeleteInspectionUseCase @Inject constructor(
    private val repository: InspectionRepository
) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}
