package com.rocketdan24.fishingstop.core.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// 문자열이 제이슨 형태인지, 제이슨 배열 형태인지
fun String?.isJsonObject():Boolean {
    if (this?.startsWith("{") == true && this.endsWith("}")==true ) {
        return true
    } else {
        return false
    }
}

fun String?.isJsonArray(): Boolean {
    if (this?.startsWith("[") == true && this.endsWith("]")==true ) {
        return true
    } else {
        return false
    }
}

fun Context.findActivity(): Activity {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> throw IllegalStateException("Activity not found")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.toKoreaLocalDateTime() =
    OffsetDateTime.parse(this)
        .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
        .toLocalDateTime()

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.toTimeAgo(): String {

    val now = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
    val diff = Duration.between(this, now)

    return when {
        diff.seconds < 60 -> "방금 전"
        diff.toMinutes() < 60 -> "${diff.toMinutes()}분 전"
        diff.toHours() < 24 -> "${diff.toHours()}시간 전"
        diff.toDays() < 7 -> "${diff.toDays()}일 전"
        else -> "${year}.${monthValue}.${dayOfMonth}"
    }
}

// 예: "2026-12-06T15:30:22.123Z" -> "2026.12.06"
fun String.toFormattedDate(): String {
    return try {
        val instant = Instant.parse(this)
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault())
            .withZone(ZoneId.systemDefault()) // 사용자의 현재 기기 타임존(KST 등) 반영
        formatter.format(instant)
    } catch (e: Exception) {
        this // 파싱 실패 시 원래 문자열 그대로 반환 (안전장치)
    }
}
//