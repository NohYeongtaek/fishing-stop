package com.rocketdan24.fishingstop.feature.inspect.data

import com.rocketdan24.fishingstop.core.database.dao.WhitelistDao
import com.rocketdan24.fishingstop.core.util.IoDispatcher
import com.rocketdan24.fishingstop.feature.inspect.domain.repository.WhitelistRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 안심 도메인 저장소 구현체.
 * 기본 내장 목록(주요 포털·금융·공공기관 공식 도메인) + DB 추가분을 합쳐 제공한다.
 */
class WhitelistRepositoryImpl @Inject constructor(
    private val whitelistDao: WhitelistDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : WhitelistRepository {

    override suspend fun getSafeDomains(): Set<String> = withContext(ioDispatcher) {
        val fromDb = whitelistDao.getAll().map { it.value.lowercase() }
        DEFAULT_SAFE_DOMAINS + fromDb
    }

    companion object {
        /** 기본 내장 안심 도메인(등록 가능 도메인 기준). 서버 연동 시 원격 목록으로 확장한다. */
        private val DEFAULT_SAFE_DOMAINS = setOf(
            "naver.com", "kakao.com", "daum.net", "google.com",
            "toss.im", "kbstar.com", "shinhan.com", "wooribank.com",
            "nonghyup.com", "ibk.co.kr", "kakaobank.com",
            "coupang.com", "gmarket.co.kr", "11st.co.kr",
            "gov.kr", "korea.kr", "hometax.go.kr", "counterscam112.go.kr",
            "police.go.kr", "fss.or.kr"
        )
    }
}
