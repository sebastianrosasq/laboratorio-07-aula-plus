package com.example.datossinmvvm.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM User ORDER BY uid ASC")
    suspend fun getAll(): List<User>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User)

    @Query(
        """
        DELETE FROM User
        WHERE uid = (SELECT MAX(uid) FROM User)
        """
    )
    suspend fun deleteLast(): Int
}
