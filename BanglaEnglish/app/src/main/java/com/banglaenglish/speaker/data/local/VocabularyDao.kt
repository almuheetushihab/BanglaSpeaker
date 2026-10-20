package com.banglaenglish.speaker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(entities: List<VocabularyEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: VocabularyEntity): Long

    @Update
    fun update(entity: VocabularyEntity): Int

    @Query("DELETE FROM vocabulary WHERE id = :id")
    fun deleteById(id: Long): Int

    @Query("SELECT * FROM vocabulary ORDER BY nextReviewTime ASC")
    fun getAllFlow(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE nextReviewTime <= :currentTime ORDER BY nextReviewTime ASC")
    fun getDueForReviewFlow(currentTime: Long = System.currentTimeMillis()): Flow<List<VocabularyEntity>>
}
