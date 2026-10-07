package com.example.datossinmvvm.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AttendanceRecordDao {
    @Query("SELECT * FROM attendance_records ORDER BY attendanceDate DESC, id DESC")
    suspend fun getAll(): List<AttendanceRecord>

    @Insert
    suspend fun insert(record: AttendanceRecord)

    @Update
    suspend fun update(record: AttendanceRecord)

    @Delete
    suspend fun delete(record: AttendanceRecord)
}
