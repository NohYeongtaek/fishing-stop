package com.example.fishingstop.feature.inspect.domain.repository

/**
 * 단축 URL 등 리다이렉트가 걸린 링크의 실제 목적지를 알아내는 리졸버.
 *
 * 페이지 콘텐츠(HTML/스크립트)는 받지 않고 리다이렉트 헤더만 따라간다 — 실행/자동 다운로드
 * 위험이 없는 "안전한 접속 확인"만 수행한다.
 */
interface UrlRedirectResolver {
    /** @return 리다이렉트를 따라간 최종 호스트. 실패(타임아웃·네트워크 오류 등) 시 null. */
    suspend fun resolveFinalHost(url: String): String?
}
