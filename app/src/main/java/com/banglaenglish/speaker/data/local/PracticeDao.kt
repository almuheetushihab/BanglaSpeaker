package com.banglaenglish.speaker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: PracticeSessionEntity): Long

    @Query("SELECT * FROM practice_sessions ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<PracticeSessionEntity>>

    @Query("SELECT COUNT(*) FROM practice_sessions")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("SELECT DISTINCT timestamp FROM practice_sessions ORDER BY timestamp DESC")
    fun getAllSessionTimestamps(): List<Long>
}
