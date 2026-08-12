package com.fantonio.entregarg.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fantonio.entregarg.data.model.Identity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdentityDao {
    @Query("SELECT * FROM identities WHERE nome LIKE '%' || :query || '%' OR cpf = :query")
    suspend fun searchIdentities(query: String): List<Identity>

    @Update
    suspend fun updateIdentity(identity: Identity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(identities: List<Identity>)

    @Query("SELECT * FROM identities")
    fun getAllIdentities(): Flow<List<Identity>>

    @Query("SELECT * FROM identities")
    suspend fun getAllList(): List<Identity>
    
    @Query("DELETE FROM identities")
    suspend fun deleteAll()
}
