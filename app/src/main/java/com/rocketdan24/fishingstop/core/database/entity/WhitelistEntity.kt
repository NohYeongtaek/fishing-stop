package com.rocketdan24.fishingstop.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 안심 도메인(화이트리스트) — 데이터 명세의 whitelist 테이블(id, label, value).
 *
 * URL 검사 시 이 목록의 도메인은 "안심 목록 등록됨"으로 안전 처리한다.
 * 기본 공식 도메인은 코드에 내장돼 있고, 이 테이블은 추후 사용자/서버 추가분을 담는다.
 *
 * @property label 표시용 이름(예: "네이버")
 * @property value 등록 가능 도메인(예: "naver.com")
 */
@Entity(tableName = "whitelist")
data class WhitelistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val label: String,
    val value: String
)
