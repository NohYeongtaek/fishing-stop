package com.example.fishingstop.features.home.domain.repositories

import com.newBie.new_bie.features.profile.domain.entities.GuestbooksEntity

interface GuestbooksRepository {
    // 방명록 조회
    suspend fun getGuestbooksList(receiverId: String, currentIndex: Int, perPage: Int): List<GuestbooksEntity>
    // 방명록 디테일
    suspend fun getGuestbook(guestbookId: Int): GuestbooksEntity?
    // 방명록 입력
    suspend fun insertGuestbook(receiverId: String, senderId: String, content: String, image: String?)
    // 방명록 삭제
    suspend fun deleteGuestbook(guestbookId: Int)
}