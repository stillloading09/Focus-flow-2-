package com.focusflow.app.data.repository

import com.focusflow.app.data.local.dao.TimeBlockDao
import com.focusflow.app.domain.model.TimeBlock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

interface TimeBlockRepository {
    fun observeBlocksForDate(date: LocalDate): Flow<List<TimeBlock>>
    fun observeBlocksInRange(start: LocalDate, end: LocalDate): Flow<List<TimeBlock>>
    suspend fun getBlock(id: Long): TimeBlock?
    suspend fun saveBlock(block: TimeBlock): Long
    suspend fun saveBlocks(blocks: List<TimeBlock>)
    suspend fun deleteBlock(block: TimeBlock)
    suspend fun setCompleted(id: Long, completed: Boolean)
}

class TimeBlockRepositoryImpl @Inject constructor(
    private val dao: TimeBlockDao
) : TimeBlockRepository {

    override fun observeBlocksForDate(date: LocalDate): Flow<List<TimeBlock>> =
        dao.observeBlocksForDate(date).map { list -> list.map { it.toDomain() } }

    override fun observeBlocksInRange(start: LocalDate, end: LocalDate): Flow<List<TimeBlock>> =
        dao.observeBlocksInRange(start, end).map { list -> list.map { it.toDomain() } }

    override suspend fun getBlock(id: Long): TimeBlock? =
        dao.getBlockById(id)?.toDomain()

    override suspend fun saveBlock(block: TimeBlock): Long =
        dao.upsert(block.toEntity())

    override suspend fun saveBlocks(blocks: List<TimeBlock>) {
        blocks.forEach { dao.upsert(it.toEntity()) }
    }

    override suspend fun deleteBlock(block: TimeBlock) =
        dao.delete(block.toEntity())

    override suspend fun setCompleted(id: Long, completed: Boolean) {
        val entity = dao.getBlockById(id) ?: return
        dao.update(entity.copy(isCompleted = completed))
    }
}
