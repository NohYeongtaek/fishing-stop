package com.rocketdan24.fishingstop.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rocketdan24.fishingstop.core.database.entity.WhitelistEntity

/** 안심 도메인(화이트리스트) DAO. */
@Dao
interface WhitelistDao {

    @Insert
    suspend fun insert(entity: WhitelistEntity)

    @Query("SELECT * FROM whitelist")
    suspend fun getAll(): List<WhitelistEntity>

    @Query("DELETE FROM whitelist WHERE id = :id")
    suspend fun deleteById(id: Long)
}
