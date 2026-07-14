package com.example.fishingstop.feature.consent.domain

import javax.inject.Inject

/**
 * 동의 결과를 저장하는 유스케이스.
 * 동의 화면에서 "동의합니다" 버튼을 눌렀을 때 호출된다.
 */
class SetConsentUseCase @Inject constructor(
    private val repository: ConsentRepository
) {
    suspend operator fun invoke(agreed: Boolean) = repository.setAgreed(agreed)
}
