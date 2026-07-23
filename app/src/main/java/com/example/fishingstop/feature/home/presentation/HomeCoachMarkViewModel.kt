package com.example.fishingstop.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fishingstop.feature.home.domain.CoachMarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 홈 화면 하단 탭 코치마크 ViewModel. 최초 1회만 노출하고, 완료 시 열람 여부를 저장한다. */
@HiltViewModel
class HomeCoachMarkViewModel @Inject constructor(
    private val coachMarkRepository: CoachMarkRepository
) : ViewModel() {

    /** 코치마크를 노출해야 하는지 여부. DataStore 조회 전에는 false(미노출)로 시작. */
    val showCoachMark: StateFlow<Boolean> = coachMarkRepository.hasSeenHomeTabsCoachMark
        .map { seen -> !seen }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** 코치마크를 끝까지 봤을 때(마지막 탭 탭 시) 열람 여부를 기록한다. */
    fun markSeen() {
        viewModelScope.launch {
            coachMarkRepository.markHomeTabsCoachMarkSeen()
        }
    }
}
