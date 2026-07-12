package com.focusflow.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.focusflow.app.data.local.dao.TimeBlockDao
import com.focusflow.app.data.local.entity.TimeBlockEntity

@Database(
    entities = [TimeBlockEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FocusFlowDatabase : RoomDatabase() {
    abstract fun timeBlockDao(): TimeBlockDao

    companion object {
        const val DATABASE_NAME = "focusflow.db"
    }
}
