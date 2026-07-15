package com.example.fishingstop.feature.onboarding.domain

import kotlinx.coroutines.flow.Flow

/**
 * 온보딩(사용설명) 열람 여부 저장소.
 * 최초 1회만 보여주고, 이후에는 건너뛰기 위해 열람 여부를 기록한다.
 */
interface OnboardingRepository {

    /** 온보딩을 이미 봤는지 여부(변경 시 자동 방출). */
    val hasSeenOnboarding: Flow<Boolean>

    /** 온보딩을 봤음을 기록한다. */
    suspend fun markOnboardingSeen()
}
