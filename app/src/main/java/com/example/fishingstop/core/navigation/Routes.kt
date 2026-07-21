package com.example.fishingstop.core.navigation

import kotlinx.serialization.Serializable

/**
 * 앱의 모든 화면 목적지(destination)를 타입세이프하게 정의한다.
 *
 * Navigation Compose 2.8+ 의 타입세이프 API를 사용하므로,
 * 문자열 route 대신 @Serializable 객체/데이터클래스로 화면을 표현한다.
 * 인자 전달도 문자열 파싱 없이 프로퍼티로 안전하게 넘길 수 있다.
 *
 * (기존 core/utils/Constants.kt 의 문자열 Routes 는 소셜앱 템플릿 잔재이며 사용하지 않는다.)
 */
sealed interface Routes {

    /** 스플래시: 최초 동의 여부를 확인해 Consent 또는 Home 으로 분기 */
    @Serializable
    data object Splash : Routes

    /**
     * 동의 화면(외부 AI 전송 안내 + 개인정보 처리방침 동의).
     * 동의는 필수이며, 거부하면 앱을 종료한다. 동의해야만 다음 화면으로 진입한다.
     */
    @Serializable
    data object Consent : Routes

    /** 온보딩(사용설명). 동의 후 최초 1회만 노출한다. */
    @Serializable
    data object Onboarding : Routes

    /** 개인정보 처리방침 전문(인앱 정적 페이지) */
    @Serializable
    data object PrivacyPolicy : Routes

    /** 홈: "검사 시작" + 하단 탭(직접검사/검사기록/예방교육/설정) */
    @Serializable
    data object Home : Routes

    /**
     * 분석 진행 화면(로딩). 입력 텍스트를 받아 AI 분석 후 결과 화면으로 교체 이동한다.
     * @param text   분석할 원문(공유/OCR/붙여넣기 등)
     * @param method 입력 방식(InspectMethod.name)
     */
    @Serializable
    data class Analyze(val text: String, val method: String) : Routes

    // ── 직접검사 방식별 화면 (FO_02 계열) ──

    /** QR 검사(FO_02_01): CameraX + ML Kit 바코드 스캔 */
    @Serializable
    data object QrScan : Routes

    /** 링크 검사(FO_02_02): URL 붙여넣기 → 휴리스틱 + AI */
    @Serializable
    data object LinkCheck : Routes

    /** 이미지 검사(FO_02_03): 포토피커 → OCR → 추출 텍스트 미리보기 → 검사 */
    @Serializable
    data object ImageCheck : Routes

    /**
     * 문자 검사(FO_02_04): 텍스트 붙여넣기.
     * @param prefill 공유(ACTION_SEND)로 진입한 경우 자동 입력할 텍스트
     */
    @Serializable
    data class TextCheck(val prefill: String? = null) : Routes

    /**
     * 검사 결과 화면. 모든 입력 방식(공유/QR/링크/이미지/텍스트)이 이 화면으로 합류한다.
     * @param inspectionId 저장된 검사 기록의 로컬 DB id (결과를 다시 로드하기 위함)
     */
    @Serializable
    data class InspectResult(val inspectionId: Long) : Routes

    /**
     * 신고 화면.
     * @param inspectionId 신고 대상 검사 기록 id
     */
    @Serializable
    data class Report(val inspectionId: Long) : Routes

    /** 예방 교육 카테고리 상세 */
    @Serializable
    data class EducationDetail(val categoryId: String) : Routes

    /** 공지사항(FO_05_01): Firestore 목록 + 당겨서 새로고침 */
    @Serializable
    data object NoticeList : Routes

    /** 관리자 화면: 숨은 PIN 게이트 통과 후 진입. 공지 작성/관리로 분기 */
    @Serializable
    data object AdminHome : Routes

    /** 공지 관리(관리자): 목록 + 상세에서 수정·삭제 */
    @Serializable
    data object NoticeManage : Routes

    /**
     * 공지 작성/수정(관리자 전용).
     * @param noticeId null이면 새 공지 작성, 값이 있으면 해당 공지 수정
     */
    @Serializable
    data class NoticeWrite(val noticeId: String? = null) : Routes
}
