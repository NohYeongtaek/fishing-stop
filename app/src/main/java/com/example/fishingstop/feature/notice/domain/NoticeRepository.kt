package com.example.fishingstop.feature.notice.domain

import com.example.fishingstop.feature.notice.domain.model.Notice

/** 공지사항 저장소. Firestore "notices" 컬렉션을 다룬다. */
interface NoticeRepository {

    /** 최신순 공지 목록을 한 번 조회한다(당겨서 새로고침 시 재호출). */
    suspend fun getNotices(): List<Notice>

    /** 단일 공지를 조회한다(수정 화면 프리필용). 없으면 null. */
    suspend fun getNotice(id: String): Notice?

    /** 공지를 등록한다(관리자). createdAt 은 서버 타임스탬프로 기록된다. */
    suspend fun addNotice(title: String, body: String)

    /** 공지 제목/본문을 수정한다(관리자). createdAt 은 유지한다. */
    suspend fun updateNotice(id: String, title: String, body: String)

    /** 공지를 삭제한다(관리자). */
    suspend fun deleteNotice(id: String)
}
