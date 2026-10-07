package com.example.datossinmvvm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentName: String,
    val classroom: String,
    val attendanceDate: String,
    val status: String
)
