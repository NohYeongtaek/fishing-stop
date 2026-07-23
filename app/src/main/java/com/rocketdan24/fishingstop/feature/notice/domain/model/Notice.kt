package com.rocketdan24.fishingstop.feature.notice.domain.model

/**
 * 공지사항 한 건.
 *
 * @param id 문서 ID(Firestore 자동생성)
 * @param title 제목
 * @param body 본문
 * @param createdAtMillis 서버 생성 시각(epoch millis). 서버 반영 전이면 0.
 */
data class Notice(
    val id: String,
    val title: String,
    val body: String,
    val createdAtMillis: Long
)
