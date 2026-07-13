package com.example.fishingstop.features.home.domain.usecase

import android.util.Log
import com.newBie.new_bie.core.utils.Constants.TAG
import com.newBie.new_bie.features.profile.domain.entities.GuestbooksEntity
import com.newBie.new_bie.features.profile.domain.repositories.GuestbooksRepository
import javax.inject.Inject

class GetGuestbookUseCase @Inject constructor(private val repository: GuestbooksRepository) {
    suspend operator fun invoke(guestbookId: Int): Result<GuestbooksEntity?>{
        return try {
            val result = repository.getGuestbook(guestbookId)
            Log.d(TAG, "GetGuestbookUseCase: $result")
            Result.success(result)
        } catch (e: Exception){
            Log.e(TAG, "GetGuestbookUseCase: ${e.message}", )
            Result.failure(e)
        }
    }
}