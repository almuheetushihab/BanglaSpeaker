package com.banglaenglish.speaker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ConversationEntity): Long

    @Update
    fun update(entity: ConversationEntity): Int

    @Query("DELETE FROM conversations WHERE id = :id")
    fun deleteById(id: Long): Int

    @Query("DELETE FROM conversations")
    fun deleteAll(): Int

    @Query("SELECT * FROM conversations ORDER BY timestamp ASC")
    fun getAllFlow(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedFlow(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE bangla LIKE '%' || :query || '%' OR english LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchFlow(query: String): Flow<List<ConversationEntity>>
}
