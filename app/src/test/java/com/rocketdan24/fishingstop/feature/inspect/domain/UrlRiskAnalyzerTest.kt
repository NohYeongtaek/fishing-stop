package com.rocketdan24.fishingstop.feature.inspect.domain

import com.rocketdan24.fishingstop.core.util.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * URL 휴리스틱 검사 단위 테스트.
 * 순수 JVM 로직이라 기기 없이 검증할 수 있다.
 */
class UrlRiskAnalyzerTest {

    private val analyzer = UrlRiskAnalyzer()

    @Test
    fun `링크가 없으면 안전으로 판정한다`() {
        val result = analyzer.analyze("오늘 저녁에 만나서 밥 먹자")
        assertEquals(RiskLevel.SAFE, result.riskLevel)
        assertEquals(0, result.riskScore)
    }

    @Test
    fun `정상 네이버 도메인은 안전으로 판정한다`() {
        val result = analyzer.analyze("기사 보기 https://blog.naver.com/notice/123")
        assertEquals(RiskLevel.SAFE, result.riskLevel)
    }

    @Test
    fun `단축 URL은 최소 주의 이상으로 판정한다`() {
        val result = analyzer.analyze("확인하세요 https://bit.ly/abc123")
        assertTrue(result.riskLevel != RiskLevel.SAFE)
        assertTrue(result.signals.any { it.contains("단축") })
    }

    @Test
    fun `네이버 사칭 유사도메인은 위험으로 판정한다`() {
        val result = analyzer.analyze("http://naver-login.top/verify")
        assertEquals(RiskLevel.DANGER, result.riskLevel)
        assertTrue(result.signals.any { it.contains("사칭") })
    }

    @Test
    fun `IP 주소 링크는 위험 신호를 남긴다`() {
        val result = analyzer.analyze("http://192.168.10.5/login")
        assertTrue(result.riskLevel != RiskLevel.SAFE)
        assertTrue(result.signals.any { it.contains("IP") })
    }

    @Test
    fun `여러 링크 중 가장 위험한 것을 기준으로 판정한다`() {
        val result = analyzer.analyze("정상 https://naver.com 그리고 http://kakao-pay.xyz/x")
        assertEquals(RiskLevel.DANGER, result.riskLevel)
        assertTrue(result.signals.any { it.contains("2개") })
    }

    @Test
    fun `점수는 0에서 100 사이로 보정된다`() {
        val result = analyzer.analyze("http://naver-login.top@1.2.3.4/xn--/verify.bit.ly")
        assertTrue(result.riskScore in 0..100)
    }

    @Test
    fun `화이트리스트 도메인은 다른 신호가 있어도 안전 처리한다`() {
        // http(비암호화)임에도 안심 목록이면 안전
        val result = analyzer.analyze(
            "http://event.naver.com/promo",
            safeDomains = setOf("naver.com")
        )
        assertEquals(RiskLevel.SAFE, result.riskLevel)
        assertEquals(0, result.riskScore)
        assertTrue(result.signals.any { it.contains("안심 목록") })
    }

    @Test
    fun `화이트리스트에 없는 유사 도메인은 여전히 위험 판정한다`() {
        val result = analyzer.analyze(
            "http://naver-login.top/verify",
            safeDomains = setOf("naver.com")
        )
        assertEquals(RiskLevel.DANGER, result.riskLevel)
    }
}
