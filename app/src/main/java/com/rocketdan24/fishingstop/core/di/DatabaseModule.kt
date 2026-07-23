package com.rocketdan24.fishingstop.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rocketdan24.fishingstop.core.database.AppDatabase
import com.rocketdan24.fishingstop.core.database.dao.InspectionDao
import com.rocketdan24.fishingstop.core.database.dao.WhitelistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Room 데이터베이스와 DAO를 앱 전역 싱글턴으로 제공한다.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** v1 → v2: 신고 완료 여부(isReported) 컬럼 추가. 기존 기록은 미신고(0)로 둔다. */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE inspection ADD COLUMN isReported INTEGER NOT NULL DEFAULT 0")
        }
    }

    /** v2 → v3: 안심 도메인(whitelist) 테이블 추가. */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS whitelist (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "label TEXT NOT NULL, " +
                    "value TEXT NOT NULL)"
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun provideInspectionDao(db: AppDatabase): InspectionDao = db.inspectionDao()

    @Provides
    fun provideWhitelistDao(db: AppDatabase): WhitelistDao = db.whitelistDao()
}
