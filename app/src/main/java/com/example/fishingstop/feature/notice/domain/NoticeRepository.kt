package com.example.fishingstop.feature.notice.domain

import com.example.fishingstop.feature.notice.domain.model.Notice

/** 공지사항 저장소. Firestore "notices" 컬렉션을 다룬다. */
interface NoticeRepository {

    /** 최신순 공지 목록을 한 번 조회한다(당겨서 새로고침 시 재호출). */
    suspend fun getNotices(): List<Notice>

    /** 공지를 등록한다(관리자). createdAt 은 서버 타임스탬프로 기록된다. */
    suspend fun addNotice(title: String, body: String)
}
