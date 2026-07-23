package com.rocketdan24.fishingstop.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rocketdan24.fishingstop.core.database.entity.InspectionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 검사 기록 접근 DAO.
 * 목록 조회는 Flow로 노출해 DB 변경 시 화면이 자동으로 갱신되도록 한다(반응형).
 */
@Dao
interface InspectionDao {

    /** 새 검사 기록 저장. 생성된 행의 id를 반환(결과 화면으로 전달용). */
    @Insert
    suspend fun insert(entity: InspectionEntity): Long

    @Update
    suspend fun update(entity: InspectionEntity)

    /** 전체 이력: 최신순 */
    @Query("SELECT * FROM inspection ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<InspectionEntity>>

    /** 즐겨찾기만: 최신순 */
    @Query("SELECT * FROM inspection WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<InspectionEntity>>

    /** 단건 조회(상세/결과 화면) */
    @Query("SELECT * FROM inspection WHERE id = :id")
    suspend fun getById(id: Long): InspectionEntity?

    /**
     * 단건 관찰(결과 화면). 즐겨찾기/신고 상태가 바뀌면 화면이 자동 갱신되도록
     * 결과 화면은 이 Flow를 구독한다.
     */
    @Query("SELECT * FROM inspection WHERE id = :id")
    fun observeById(id: Long): Flow<InspectionEntity?>

    @Query("UPDATE inspection SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    /** 신고 완료 표시(재신고 방지) */
    @Query("UPDATE inspection SET isReported = 1 WHERE id = :id")
    suspend fun markReported(id: Long)

    @Query("DELETE FROM inspection WHERE id = :id")
    suspend fun deleteById(id: Long)
}
