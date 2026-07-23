package com.rocketdan24.fishingstop.feature.report.domain

import com.rocketdan24.fishingstop.feature.report.domain.model.IndicatorType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 신고 지표 자동추출 단위 테스트. */
class ExtractIndicatorsUseCaseTest {

    private val useCase = ExtractIndicatorsUseCase()

    @Test
    fun `URL을 링크 지표로 추출한다`() {
        val result = useCase("확인하세요 https://bit.ly/abc123 지금")
        assertTrue(result.any { it.type == IndicatorType.URL && it.value.contains("bit.ly/abc123") })
    }

    @Test
    fun `휴대폰 번호를 전화번호 지표로 추출한다`() {
        val result = useCase("문의 010-1234-5678 로 연락주세요")
        assertTrue(result.any { it.type == IndicatorType.PHONE && it.value == "010-1234-5678" })
    }

    @Test
    fun `지표가 없으면 빈 목록을 반환한다`() {
        val result = useCase("오늘 저녁에 만나서 밥 먹자")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `같은 값은 중복 없이 한 번만 추출한다`() {
        val result = useCase("http://evil.top 그리고 또 http://evil.top")
        assertEquals(1, result.count { it.type == IndicatorType.URL })
    }

    @Test
    fun `전화번호는 계좌번호로 중복 추출되지 않는다`() {
        // 010-1234-5678 은 전화번호 형식이자 계좌 정규식에도 걸릴 수 있으나 전화번호로만 잡혀야 한다.
        val result = useCase("010-1234-5678")
        assertEquals(1, result.size)
        assertEquals(IndicatorType.PHONE, result.first().type)
    }
}
