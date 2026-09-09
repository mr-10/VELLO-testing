package com.mr10.vello.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun String?.toFormattedTime(): String {
    if (this == null) return ""
    return try {
        val instant = Instant.parse(this)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        localDateTime.format(formatter)
    } catch (e: Exception) {
        ""
    }
}

fun String?.toFormattedLastSeen(): String {
    if (this == null) return "Offline"
    return try {
        val instant = Instant.parse(this)
        val now = Instant.now()
        val minutes = ChronoUnit.MINUTES.between(instant, now)
        val hours = ChronoUnit.HOURS.between(instant, now)
        val days = ChronoUnit.DAYS.between(instant, now)

        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days == 1L -> "Yesterday"
            else -> {
                val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                localDateTime.format(formatter)
            }
        }
    } catch (e: Exception) {
        "Offline"
    }
}
