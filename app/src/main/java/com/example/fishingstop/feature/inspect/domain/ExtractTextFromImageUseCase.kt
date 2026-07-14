package com.example.fishingstop.feature.inspect.domain

import android.net.Uri
import javax.inject.Inject

/**
 * 이미지에서 텍스트를 추출하는 유스케이스(OCR).
 *
 * 이후 단계(이미지 검사)는 이 결과 텍스트를 그대로 [AnalyzeMessageUseCase] 로 넘겨
 * 메시지 검사와 동일한 분석 로직을 재사용한다.
 *
 * @return 성공 시 인식된 텍스트. 비어 있으면(글자 없음) Result.failure 로 처리해
 *         호출부가 "글자를 찾지 못했어요" 안내를 하도록 한다.
 */
class ExtractTextFromImageUseCase @Inject constructor(
    private val ocrRepository: OcrRepository
) {
    suspend operator fun invoke(imageUri: Uri): Result<String> = runCatching {
        val text = ocrRepository.extractText(imageUri).trim()
        check(text.isNotEmpty()) { "이미지에서 글자를 찾지 못했습니다." }
        text
    }
}
