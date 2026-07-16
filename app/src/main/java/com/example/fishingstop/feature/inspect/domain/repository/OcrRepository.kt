package com.example.fishingstop.feature.inspect.domain.repository

import android.net.Uri

/**
 * 이미지에서 텍스트를 추출(OCR)하는 저장소 인터페이스.
 *
 * 온디바이스(ML Kit)로 처리해 이미지 원본을 외부로 전송하지 않는다.
 * 추출된 "텍스트"만 이후 분석 단계(AnalyzeMessageUseCase)로 넘어간다.
 */
interface OcrRepository {

    /**
     * @param imageUri 갤러리/공유로 받은 이미지의 Uri
     * @return 인식된 텍스트(줄바꿈 포함). 인식 결과가 없으면 빈 문자열.
     */
    suspend fun extractText(imageUri: Uri): String
}