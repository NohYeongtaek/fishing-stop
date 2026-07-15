package com.example.fishingstop.core.util

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 예외를 사용자에게 보여줄 한국어 안내 문구로 변환한다.
 *
 * 원칙(기획 체크리스트: "모든 검사 실패 케이스의 사용자 안내 문구 정의"):
 * - 영어 원문(e.message)이나 기술 용어를 그대로 노출하지 않는다.
 * - 우리가 한국어로 직접 만든 메시지(IllegalState 등)는 그대로 사용한다.
 */
fun Throwable.toUserMessage(): String {
    // 우리가 의도적으로 던진 한국어 메시지는 그대로 보여준다.
    val msg = message.orEmpty()
    if (msg.any { it in '가'..'힣' }) return msg

    return when (this) {
        is UnknownHostException, is SocketTimeoutException ->
            "네트워크 연결을 확인한 뒤 다시 시도해 주세요."
        is IOException ->
            "서버와 통신하지 못했어요. 잠시 후 다시 시도해 주세요."
        else -> when {
            // Firebase App Check / 인증 관련
            msg.contains("App Check", ignoreCase = true) ||
                msg.contains("PERMISSION_DENIED", ignoreCase = true) ->
                "서버 인증에 실패했어요. 앱을 최신 상태로 유지하고 잠시 후 다시 시도해 주세요."
            // Gemini 모델/쿼터 관련
            msg.contains("quota", ignoreCase = true) || msg.contains("overloaded", ignoreCase = true) ->
                "분석 서비스가 혼잡해요. 잠시 후 다시 시도해 주세요."
            else -> "요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요."
        }
    }
}
