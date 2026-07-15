package com.example.fishingstop.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room은 List<String> 같은 컬렉션을 바로 저장하지 못하므로,
 * kotlinx-serialization으로 JSON 문자열 <-> List<String> 변환을 담당한다.
 */
class StringListConverter {

    @TypeConverter
    fun fromList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String): List<String> =
        if (value.isBlank()) emptyList() else Json.decodeFromString(value)
}
