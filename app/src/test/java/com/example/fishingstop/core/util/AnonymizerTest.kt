package com.example.fishingstop.core.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 개인정보 마스킹 단위 테스트. */
class AnonymizerTest {

    @Test
    fun `전화번호를 마스킹한다`() {
        val masked = Anonymizer.mask("연락처 010-1234-5678 로 전화주세요")
        assertFalse(masked.contains("1234"))
        assertTrue(masked.contains("[전화번호]"))
    }

    @Test
    fun `주민번호를 마스킹한다`() {
        val masked = Anonymizer.mask("주민번호 900101-1234567 입니다")
        assertFalse(masked.contains("1234567"))
        assertTrue(masked.contains("[주민번호]"))
    }

    @Test
    fun `이메일을 마스킹한다`() {
        val masked = Anonymizer.mask("메일은 test.user@example.com")
        assertFalse(masked.contains("test.user@example.com"))
        assertTrue(masked.contains("[이메일]"))
    }

    @Test
    fun `계좌번호 같은 긴 숫자열을 마스킹한다`() {
        val masked = Anonymizer.mask("입금계좌 110-234-567890")
        assertTrue(masked.contains("[숫자]"))
    }

    @Test
    fun `일반 문장은 그대로 둔다`() {
        val text = "오늘 오후에 만나요"
        assertTrue(Anonymizer.mask(text) == text)
    }
}
