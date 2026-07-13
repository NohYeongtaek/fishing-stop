package com.example.fishingstop.features.home.data.repositories

import com.newBie.new_bie.core.utils.getGridRange
import com.newBie.new_bie.features.profile.data.datasources.GuestbooksDatasource
import com.newBie.new_bie.features.profile.data.dto.GuestbooksDto
import com.newBie.new_bie.features.profile.domain.entities.GuestbooksEntity
import com.newBie.new_bie.features.profile.domain.repositories.GuestbooksRepository
import javax.inject.Inject

class GuestbooksRepositoryImpl @Inject constructor(private val datasource: GuestbooksDatasource): GuestbooksRepository {

    override suspend fun getGuestbooksList(
        receiverId: String,
        currentIndex: Int,
        perPage: Int
    ): List<GuestbooksEntity> {
        // UtilFunctions에서 확장 함수 미리 만들어놓음
        val rangeString = getGridRange(currentIndex, perPage)

        val dtoList = datasource.getGuestbooksList(
            receiverId = receiverId,
            range = rangeString
        )
        return dtoList.map { it.toEntity() }
    }

    override suspend fun getGuestbook(guestbookId: Int): GuestbooksEntity? {
        val result = datasource.getGuestbook(
            guestbookId = guestbookId
        )
        return result?.toEntity()
    }

    override suspend fun insertGuestbook(
        receiverId: String,
        senderId: String,
        content: String,
        image: String?
    ) {
        datasource.insertGuestbook(
            receiverId = receiverId,
            senderId = senderId,
            content = content,
            image = image
        )
    }

    override suspend fun deleteGuestbook(guestbookId: Int) {
        datasource.deleteGuestbook(guestbookId)
    }
}