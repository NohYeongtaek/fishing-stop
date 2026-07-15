package com.example.fishingstop.feature.inspect.domain

import com.example.fishingstop.feature.inspect.domain.model.InspectionResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 검사 이력을 관찰하는 유스케이스.
 * @param favoritesOnly true면 즐겨찾기만, false면 전체(최신순).
 */
class ObserveInspectionsUseCase @Inject constructor(
    private val repository: InspectionRepository
) {
    operator fun invoke(favoritesOnly: Boolean): Flow<List<InspectionResult>> =
        if (favoritesOnly) repository.observeFavorites() else repository.observeHistory()
}
