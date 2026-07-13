package com.example.fishingstop.features.home.data.dto

import com.google.gson.annotations.SerializedName
import com.newBie.new_bie.features.post.data.dto.UserDto
import com.newBie.new_bie.features.post.data.mapper.toEntity
import com.newBie.new_bie.features.profile.domain.entities.GuestbooksEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class GuestbooksDto(
    val id: Int,
    @SerializedName("receiver_id") val receiverId: UserDto,
    @SerializedName("sender_id") val senderId: UserDto,
    val title: String,
    val content: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("deleted_at") val deletedAt: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("comments_count") val commentsCount: Int
){
    fun toEntity(): GuestbooksEntity{
        return GuestbooksEntity(
            id = id,
            receiverId = receiverId.toEntity(),
            senderId = senderId.toEntity(),
            title = title,
            content = content,
            imageUrl = imageUrl,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            createdAt = createdAt,
            commentsCount = commentsCount
        )
    }
}
