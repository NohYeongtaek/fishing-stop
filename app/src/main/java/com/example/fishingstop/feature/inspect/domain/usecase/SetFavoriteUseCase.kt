package com.example.fishingstop.feature.inspect.domain.usecase

import com.example.fishingstop.feature.inspect.domain.repository.InspectionRepository
import javax.inject.Inject

/** 검사 기록의 즐겨찾기 상태를 변경하는 유스케이스. */
class SetFavoriteUseCase @Inject constructor(
    private val repository: InspectionRepository
) {
    suspend operator fun invoke(id: Long, favorite: Boolean) =
        repository.setFavorite(id, favorite)
}
