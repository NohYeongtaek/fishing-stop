package com.example.fishingstop.feature.inspect.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishingstop.feature.inspect.domain.usecase.DeleteInspectionUseCase
import com.example.fishingstop.feature.inspect.domain.usecase.ObserveInspectionsUseCase
import com.example.fishingstop.feature.inspect.domain.usecase.SetFavoriteUseCase
import com.example.fishingstop.feature.inspect.domain.model.InspectionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 검사 기록 화면 ViewModel.
 * 필터(전체/즐겨찾기)에 따라 Room의 Flow를 구독해 리스트를 노출한다(반응형: 변경 시 자동 갱신).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val observeInspections: ObserveInspectionsUseCase,
    private val setFavoriteUseCase: SetFavoriteUseCase,
    private val deleteInspectionUseCase: DeleteInspectionUseCase
) : ViewModel() {

    private val _favoritesOnly = MutableStateFlow(false)
    val favoritesOnly: StateFlow<Boolean> = _favoritesOnly.asStateFlow()

    /** 필터가 바뀌면 해당 Flow로 자동 전환된다. */
    val items: StateFlow<List<InspectionResult>> =
        _favoritesOnly
            .flatMapLatest { favOnly -> observeInspections(favOnly) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun setFavoritesOnly(value: Boolean) {
        _favoritesOnly.value = value
    }

    fun toggleFavorite(item: InspectionResult) {
        viewModelScope.launch {
            setFavoriteUseCase(item.id, !item.isFavorite)
        }
    }

    /** 기록 삭제(기기 내 로컬 데이터만 지워진다). */
    fun delete(item: InspectionResult) {
        viewModelScope.launch {
            deleteInspectionUseCase(item.id)
        }
    }
}
