package com.rocketdan24.fishingstop.feature.consent.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 동의 여부를 관찰하는 유스케이스.
 * 스플래시 화면이 이 값을 구독해 Consent/Home 으로 분기한다.
 */
class ObserveConsentUseCase @Inject constructor(
    private val repository: ConsentRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.hasAgreed
}
