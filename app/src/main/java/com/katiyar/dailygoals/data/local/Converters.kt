package com.katiyar.dailygoals.data.local

import androidx.room.TypeConverter
import com.katiyar.dailygoals.domain.model.Priority
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun localTimeToString(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter
    fun priorityToString(value: Priority?): String? = value?.name

    @TypeConverter
    fun stringToPriority(value: String?): Priority? =
        value?.let { runCatching { Priority.valueOf(it) }.getOrDefault(Priority.MEDIUM) }
}
