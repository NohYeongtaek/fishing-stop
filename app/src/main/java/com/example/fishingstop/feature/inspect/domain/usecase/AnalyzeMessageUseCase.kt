package com.example.fishingstop.feature.inspect.domain.usecase

import com.example.fishingstop.core.util.InspectMethod
import com.example.fishingstop.feature.inspect.domain.repository.InspectionRepository
import javax.inject.Inject

/**
 * 메시지(텍스트)를 검사하는 유스케이스.
 *
 * 공유(ACTION_SEND), 텍스트 붙여넣기, OCR 결과, URL 검사 등 모든 텍스트 기반 입력이
 * 최종적으로 이 유스케이스를 통해 동일한 분석 로직을 탄다.
 *
 * @return 성공 시 저장된 검사 기록 id. 실패(네트워크/파싱 등)는 Result.failure 로 전달.
 */
class AnalyzeMessageUseCase @Inject constructor(
    private val repository: InspectionRepository
) {
    suspend operator fun invoke(text: String, method: InspectMethod): Result<Long> =
        runCatching { repository.analyzeAndSave(text = text.trim(), method = method) }
}
