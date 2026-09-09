package com.mr10.vello.data.local.dao

import androidx.room.*
import com.mr10.vello.data.local.entities.UserConnectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM user_connections")
    fun getAllConnections(): Flow<List<UserConnectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: UserConnectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnections(connections: List<UserConnectionEntity>)

    @Delete
    suspend fun deleteConnection(connection: UserConnectionEntity)

    @Query("DELETE FROM user_connections")
    suspend fun deleteAllConnections()
}
