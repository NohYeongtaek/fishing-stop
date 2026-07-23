package com.rocketdan24.fishingstop.feature.report.domain

import com.rocketdan24.fishingstop.feature.inspect.domain.UrlRiskAnalyzer
import com.rocketdan24.fishingstop.feature.report.domain.model.Indicator
import com.rocketdan24.fishingstop.feature.report.domain.model.IndicatorSource
import com.rocketdan24.fishingstop.feature.report.domain.model.IndicatorType
import javax.inject.Inject

/**
 * 원문에서 신고 대상 "지표"를 자동 추출한다(순수 JVM — 단위 테스트 용이).
 *
 * - URL: [UrlRiskAnalyzer.URL_REGEX] 를 재사용한다(중복 구현 방지).
 * - 전화번호: 휴대폰 번호 패턴.
 * - 계좌번호: 하이픈으로 구분된 숫자 그룹(오탐이 있을 수 있어 전화번호로 잡힌 값은 제외).
 *
 * SNS 계정·앱 이름은 자동추출 정확도가 낮아 다루지 않고, 사용자가 직접 입력하도록 둔다.
 * 추출 결과는 모두 source=AUTO 이며, 실제 신고 포함 여부는 사용자가 화면에서 선택한다.
 */
class ExtractIndicatorsUseCase @Inject constructor() {

    operator fun invoke(text: String): List<Indicator> {
        val result = mutableListOf<Indicator>()

        // 1) URL
        UrlRiskAnalyzer.URL_REGEX.findAll(text)
            .map { it.value }
            .distinct()
            .forEach { result += Indicator(IndicatorType.URL, it, IndicatorSource.AUTO) }

        // 2) 전화번호(휴대폰)
        val phones = PHONE_REGEX.findAll(text).map { it.value }.distinct().toList()
        phones.forEach { result += Indicator(IndicatorType.PHONE, it, IndicatorSource.AUTO) }

        // 3) 계좌번호 후보(전화번호로 이미 잡힌 값은 제외해 중복/오탐 감소)
        ACCOUNT_REGEX.findAll(text)
            .map { it.value }
            .distinct()
            .filter { it !in phones }
            .forEach { result += Indicator(IndicatorType.ACCOUNT, it, IndicatorSource.AUTO) }

        // 같은 (종류, 값)이 여러 번 나오면 하나만
        return result.distinctBy { it.type to it.value }
    }

    companion object {
        // 휴대폰: 010-1234-5678 / 01012345678 등
        private val PHONE_REGEX = Regex("01[0-9][- ]?\\d{3,4}[- ]?\\d{4}")

        // 계좌: 하이픈으로 구분된 3그룹 숫자(대략). 전화번호와 형태가 겹칠 수 있어 후처리로 걸러낸다.
        private val ACCOUNT_REGEX = Regex("\\d{2,6}-\\d{2,6}-\\d{2,7}")
    }
}
