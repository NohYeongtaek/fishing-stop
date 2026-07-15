package com.example.fishingstop.core.util

/**
 * 검사를 시작한 입력 방식.
 * 검사 기록(이력)에서 "어떤 경로로 검사했는지" 필터/표시하는 데 사용한다.
 */
enum class InspectMethod(val label: String) {
    /** 메시지 앱에서 공유(ACTION_SEND)로 넘어온 텍스트 */
    SHARE("공유"),

    /** 직접검사 탭: QR 코드 촬영 */
    QR("QR"),

    /** 직접검사 탭: 링크 붙여넣기 */
    LINK("링크"),

    /** 직접검사 탭: 이미지 업로드(OCR) */
    IMAGE("이미지"),

    /** 직접검사 탭: 텍스트 붙여넣기 */
    TEXT("텍스트")
}
