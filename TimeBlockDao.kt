package com.focusflow.app.data.local.dao

import androidx.room.*
import com.focusflow.app.data.local.entity.TimeBlockEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TimeBlockDao {

    @Query("SELECT * FROM time_blocks WHERE date = :date ORDER BY startTime ASC")
    fun observeBlocksForDate(date: LocalDate): Flow<List<TimeBlockEntity>>

    @Query("SELECT * FROM time_blocks WHERE id = :id")
    suspend fun getBlockById(id: Long): TimeBlockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(block: TimeBlockEntity): Long

    @Update
    suspend fun update(block: TimeBlockEntity)

    @Delete
    suspend fun delete(block: TimeBlockEntity)

    @Query("DELETE FROM time_blocks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM time_blocks WHERE date BETWEEN :start AND :end ORDER BY date, startTime")
    fun observeBlocksInRange(start: LocalDate, end: LocalDate): Flow<List<TimeBlockEntity>>
}
