package dev.ninjacheetah.tigerdine.util

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Instant.formatTigerDine(): String {
    val zone = TimeZone.of("America/New_York")
    val localDateTime = this.toLocalDateTime(zone)
    return "%02d:%02d".format(localDateTime.hour, localDateTime.minute)
}

fun Instant.formatVisitingChef(): String {
    val timeZone = TimeZone.of("America/New_York")
    val date = toLocalDateTime(timeZone).date

    val weekday = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Monday"
        DayOfWeek.TUESDAY -> "Tuesday"
        DayOfWeek.WEDNESDAY -> "Wednesday"
        DayOfWeek.THURSDAY -> "Thursday"
        DayOfWeek.FRIDAY -> "Friday"
        DayOfWeek.SATURDAY -> "Saturday"
        DayOfWeek.SUNDAY -> "Sunday"
    }

    val month = when (date.month) {
        Month.JANUARY -> "Jan"
        Month.FEBRUARY -> "Feb"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Apr"
        Month.MAY -> "May"
        Month.JUNE -> "Jun"
        Month.JULY -> "Jul"
        Month.AUGUST -> "Aug"
        Month.SEPTEMBER -> "Sep"
        Month.OCTOBER -> "Oct"
        Month.NOVEMBER -> "Nov"
        Month.DECEMBER -> "Dec"
    }

    return "$weekday, $month ${date.day}"
}

fun LocalDateTime.toYyyyMmDd(): String =
    "%04d-%02d-%02d".format(year, month.number, day)

fun LocalDateTime.toFDMPAPIFriendly(): String =
    "%04d/%02d/%02d".format(year, month.number, day)
