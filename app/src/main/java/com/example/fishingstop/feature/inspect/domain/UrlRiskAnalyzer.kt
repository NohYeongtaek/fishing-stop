package com.example.fishingstop.feature.inspect.domain

import com.example.fishingstop.core.util.RiskLevel
import com.example.fishingstop.feature.inspect.domain.model.RiskAnalysis
import java.net.URI
import javax.inject.Inject

/**
 * 메시지 속 URL을 규칙 기반(휴리스틱)으로 검사한다.
 *
 * AI를 쓰지 않으므로 오프라인·무비용으로 즉시 동작한다. URL 검사는 위험도 판정의
 * 핵심 영역이므로 규칙 추가/가중치 변경은 반드시 코드 리뷰를 거친다(협업 규칙).
 *
 * 순수 Kotlin/JVM만 사용(Android 의존 없음) → 단위 테스트가 쉽다.
 */
class UrlRiskAnalyzer @Inject constructor() {

    /**
     * @param text 링크가 포함된 원문(또는 URL 자체)
     * @param safeDomains 안심 도메인(화이트리스트) — 이 목록의 도메인은 안전 처리
     * @return 휴리스틱 점수/등급/근거로 채운 분석 결과
     */
    fun analyze(text: String, safeDomains: Set<String> = emptySet()): RiskAnalysis {
        val urls = extractUrls(text)
        if (urls.isEmpty()) {
            return RiskAnalysis(
                riskScore = 0,
                riskLevel = RiskLevel.SAFE,
                signals = listOf("메시지에서 링크(URL)를 찾지 못했습니다."),
                advice = "링크가 없어 URL 위험 신호는 확인되지 않았습니다. 내용 자체가 의심되면 텍스트 검사를 이용하세요."
            )
        }

        // 가장 위험한 링크 하나를 대표로 삼는다(여러 개면 최악의 것 기준).
        val worst = urls.map { scoreUrl(it, safeDomains) }.maxByOrNull { it.score }!!
        val score = worst.score.coerceIn(0, 100)

        val signals = buildList {
            if (urls.size > 1) add("메시지에 링크가 ${urls.size}개 포함되어 있습니다.")
            addAll(worst.signals)
            if (worst.signals.isEmpty()) add("특별한 위험 신호는 발견되지 않았습니다.")
        }.take(5)

        return RiskAnalysis(
            riskScore = score,
            riskLevel = RiskLevel.fromScore(score),
            signals = signals,
            advice = adviceFor(RiskLevel.fromScore(score))
        )
    }

    // ── 한 URL에 대한 점수/근거 ──
    private data class UrlScore(val score: Int, val signals: List<String>)

    private fun scoreUrl(raw: String, safeDomains: Set<String>): UrlScore {
        val signals = mutableListOf<String>()
        var score = 0

        // scheme 없으면 파싱을 위해 붙여준다.
        val normalized = if (raw.contains("://")) raw else "http://$raw"
        val uri = runCatching { URI(normalized) }.getOrNull()
        val host = uri?.host?.lowercase()
            ?: return UrlScore(40, listOf("형식이 이상한 링크입니다."))

        // 0) 안심 목록(화이트리스트): 공식 도메인이면 안전 처리하고 나머지 검사를 건너뛴다.
        if (registrableDomain(host) in safeDomains) {
            return UrlScore(0, listOf("안심 목록에 등록된 공식 도메인입니다."))
        }

        // 1) http(비암호화)
        if (normalized.startsWith("http://", ignoreCase = true)) {
            score += 10
            signals += "암호화되지 않은(http) 링크입니다."
        }
        // 2) IP 주소를 도메인 대신 사용
        if (host.matches(IP_REGEX)) {
            score += 40
            signals += "도메인 대신 IP 주소를 사용합니다."
        }
        // 3) 사용자정보(@) 위장
        if (!uri.userInfo.isNullOrEmpty()) {
            score += 35
            signals += "'@' 기호로 실제 주소를 감추는 형태입니다."
        }
        // 4) 퓨니코드(유사 문자 위장)
        if (host.contains("xn--")) {
            score += 35
            signals += "유사 문자(퓨니코드) 도메인일 수 있습니다."
        }
        // 5) 단축 URL(목적지 은닉)
        val shortener = SHORTENERS.firstOrNull { host == it || host.endsWith(".$it") }
        if (shortener != null) {
            score += 35
            signals += "단축 URL($shortener) — 실제 연결 주소가 감춰져 있습니다."
        }
        // 6) 위험도 높은 최상위 도메인
        val badTld = SUSPICIOUS_TLDS.firstOrNull { host.endsWith(".$it") }
        if (badTld != null) {
            score += 25
            signals += "주의가 필요한 도메인(.$badTld) 입니다."
        }
        // 7) 기관/브랜드 사칭 의심(유사 도메인)
        val impersonated = detectImpersonation(host)
        if (impersonated != null) {
            score += 45
            signals += "'$impersonated' 사칭이 의심되는 도메인입니다."
        }
        // 8) 비정상적으로 복잡한 도메인
        val labels = host.split('.')
        if (labels.size >= 5 || host.count { it == '-' } >= 3) {
            score += 10
            signals += "도메인 구조가 비정상적으로 복잡합니다."
        }

        return UrlScore(score.coerceIn(0, 100), signals)
    }

    /** 브랜드 토큰이 호스트에 있는데 등록 가능 도메인이 공식 도메인이 아니면 사칭 의심. */
    private fun detectImpersonation(host: String): String? {
        val registrable = registrableDomain(host)
        for ((token, official) in BRAND_OFFICIAL) {
            if (host.contains(token) && official.none { registrable == it }) {
                return token
            }
        }
        return null
    }

    /** 등록 가능 도메인(대략). 한국 2단계 도메인(co.kr 등)을 보정한다. */
    private fun registrableDomain(host: String): String {
        val labels = host.split('.')
        if (labels.size < 2) return host
        val lastTwo = labels.takeLast(2).joinToString(".")
        val secondLevel = labels.takeLast(2).first()
        return if (labels.size >= 3 && lastTwo.endsWith(".kr") && secondLevel in KR_SECOND_LEVELS) {
            labels.takeLast(3).joinToString(".")
        } else {
            lastTwo
        }
    }

    private fun adviceFor(level: RiskLevel): String = when (level) {
        RiskLevel.DANGER ->
            "이 링크를 절대 열지 마세요. 기관·서비스는 문자 링크로 로그인/인증을 요구하지 않습니다. 공식 앱이나 공식 홈페이지로 직접 접속해 확인하세요."
        RiskLevel.WARNING ->
            "링크의 실제 주소와 출처를 반드시 확인하세요. 조금이라도 의심되면 열지 말고 공식 채널로 확인하는 것이 안전합니다."
        RiskLevel.SAFE ->
            "뚜렷한 위험 신호는 없지만, 링크는 언제나 신중하게 확인하세요."
    }

    private fun extractUrls(text: String): List<String> =
        URL_REGEX.findAll(text).map { it.value }.distinct().toList()

    companion object {
        // scheme이 있으면 공백 전까지 통째로(IP·퓨니코드·@위장 포함), 없으면 알파벳 TLD 도메인만 추출
        private val URL_REGEX =
            Regex("(?i)(?:https?://[^\\s]+|\\b(?:[a-z0-9\\-]+\\.)+[a-z]{2,}(?::\\d+)?(?:/[^\\s]*)?)")

        private val IP_REGEX = Regex("^\\d{1,3}(\\.\\d{1,3}){3}$")

        // 대표적인 단축 URL 서비스(국내외)
        private val SHORTENERS = setOf(
            "bit.ly", "tinyurl.com", "goo.gl", "t.co", "is.gd", "ow.ly",
            "buly.kr", "me2.do", "han.gl", "vo.la", "url.kr", "abr.ge"
        )

        // 스미싱에 자주 쓰이는 최상위 도메인
        private val SUSPICIOUS_TLDS = setOf(
            "zip", "top", "xyz", "tk", "gq", "cf", "ml", "work", "click",
            "loan", "country", "kim", "cn", "ru", "su"
        )

        private val KR_SECOND_LEVELS = setOf("co", "go", "or", "ne", "re", "pe", "ac")

        // 사칭 대상 브랜드/기관 → 공식 등록 도메인 목록
        private val BRAND_OFFICIAL = mapOf(
            "naver" to listOf("naver.com"),
            "kakao" to listOf("kakao.com", "kakaocorp.com", "daum.net"),
            "toss" to listOf("toss.im"),
            "kbstar" to listOf("kbstar.com"),
            "shinhan" to listOf("shinhan.com"),
            "wooribank" to listOf("wooribank.com"),
            "nonghyup" to listOf("nonghyup.com"),
            "coupang" to listOf("coupang.com"),
            "hometax" to listOf("hometax.go.kr")
        )
    }
}
