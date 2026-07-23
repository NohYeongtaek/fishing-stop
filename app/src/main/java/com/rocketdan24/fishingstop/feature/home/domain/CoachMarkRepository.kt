package com.rocketdan24.fishingstop.feature.home.domain

import kotlinx.coroutines.flow.Flow

/**
 * 홈 화면 하단 탭 코치마크 열람 여부 저장소.
 * 최초 1회만 보여주고, 이후에는 건너뛰기 위해 열람 여부를 기록한다.
 */
interface CoachMarkRepository {

    /** 홈 탭 코치마크를 이미 봤는지 여부(변경 시 자동 방출). */
    val hasSeenHomeTabsCoachMark: Flow<Boolean>

    /** 홈 탭 코치마크를 봤음을 기록한다. */
    suspend fun markHomeTabsCoachMarkSeen()
}
