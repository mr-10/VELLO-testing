package com.mr10.vello.data.local.dao

import androidx.room.*
import com.mr10.vello.data.local.entities.CallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    @Query("SELECT * FROM calls ORDER BY created_at DESC")
    fun getAllCalls(): Flow<List<CallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallEntity)

    @Delete
    suspend fun deleteCall(call: CallEntity)

    @Query("DELETE FROM calls")
    suspend fun deleteAllCalls()
}
