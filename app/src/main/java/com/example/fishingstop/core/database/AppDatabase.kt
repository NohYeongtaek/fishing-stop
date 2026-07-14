package com.example.fishingstop.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fishingstop.core.database.converter.StringListConverter
import com.example.fishingstop.core.database.dao.InspectionDao
import com.example.fishingstop.core.database.dao.WhitelistDao
import com.example.fishingstop.core.database.entity.InspectionEntity
import com.example.fishingstop.core.database.entity.WhitelistEntity

/**
 * 앱 로컬 데이터베이스.
 * 스키마 변경 시 version을 올리고 마이그레이션을 추가한다.
 */
@Database(
    entities = [InspectionEntity::class, WhitelistEntity::class],
    version = 3, // v2: inspection.isReported 추가 / v3: whitelist 테이블 추가
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inspectionDao(): InspectionDao
    abstract fun whitelistDao(): WhitelistDao

    companion object {
        const val NAME = "fishingstop.db"
    }
}
