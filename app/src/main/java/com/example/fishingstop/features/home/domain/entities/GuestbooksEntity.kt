package com.example.fishingstop.features.home.domain.entities

import com.newBie.new_bie.features.post.domain.entities.UserEntity

data class GuestbooksEntity (
    val id: Int,
    val receiverId: UserEntity,
    val senderId: UserEntity,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val updatedAt: String?,
    val deletedAt: String?,
    val createdAt: String,
    val commentsCount: Int
    )