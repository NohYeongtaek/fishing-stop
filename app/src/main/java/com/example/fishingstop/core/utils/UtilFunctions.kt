package com.newBie.new_bie.core.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun getRange(currentIndex : Int, perPage : Int = 5) : String {
    var startIndex : Int = currentIndex - 1
    var endIndex : Int = perPage - 1

    if (currentIndex != 1) {
        endIndex = (currentIndex * perPage) - 1
        startIndex = (currentIndex - 1) * perPage
    }
    val range : String = "${startIndex}-${endIndex}"
    return range
}

fun getGridRange(currentIndex: Int, perPage: Int = 5): String {
    // 예: 현재 12개 있다면(currentIndex=12), 12번부터 23번까지(총 12개) 가져옴
    val startIndex = currentIndex
    val endIndex = currentIndex + perPage - 1

    return "${startIndex}-${endIndex}"
}

@RequiresApi(Build.VERSION_CODES.O)
fun timeAgo(dateTime: LocalDateTime): String {

    val now = LocalDateTime.now()
    val diff = Duration.between(dateTime, now)

    return when {
        diff.seconds < 60 -> {
            "방금 전"
        }

        diff.toMinutes() < 60 -> {
            "${diff.toMinutes()}분 전"
        }

        diff.toHours() < 24 -> {
            "${diff.toHours()}시간 전"
        }

        diff.toDays() < 7 -> {
            "${diff.toDays()}일 전"
        }

        else -> {
            "${dateTime.year}.${dateTime.monthValue}.${dateTime.dayOfMonth}"
        }
    }
}