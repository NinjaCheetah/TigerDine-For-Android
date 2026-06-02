package dev.ninjacheetah.tigerdine.components

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Instant.formatTigerDine(): String {
    val zone = TimeZone.of("America/New_York")
    val ldt = this.toLocalDateTime(zone)
    return "%02d:%02d".format(ldt.hour, ldt.minute)
}

fun LocalDateTime.toYyyyMmDd(): String =
    "%04d-%02d-%02d".format(year, month.number, day)
