package dev.ninjacheetah.tigerdine.util

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

fun Instant.formatTigerDine(
    use24Hour: Boolean
): String {
//    val zone = TimeZone.of("America/New_York")
//    val localDateTime = this.toLocalDateTime(zone)
//    return "%02d:%02d".format(localDateTime.hour, localDateTime.minute)
    val localDateTime = this.toLocalDateTime(
        TimeZone.of("America/New_York")
    )

    return if (use24Hour) {
        "%02d:%02d".format(
            localDateTime.hour,
            localDateTime.minute
        )
    } else {
        val hour12 = when (val h = localDateTime.hour % 12) {
            0 -> 12
            else -> h
        }

        "%d:%02d %s".format(
            hour12,
            localDateTime.minute,
            if (localDateTime.hour < 12) "AM" else "PM"
        )
    }
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

fun Instant.formatNextOpen(
    use24Hour: Boolean
): String {
    val timeZone = TimeZone.of("America/New_York")
    val date = this.toLocalDateTime(timeZone).date

    val weekday = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }

    return "${this.formatTigerDine(use24Hour)} $weekday"
}

fun Instant.formatLastRefreshed(
    use24Hour: Boolean
): String {
    val timeZone = TimeZone.of("America/New_York")
    val date = this.toLocalDateTime(timeZone).date

    return "%02d/%02d/%04d, ".format(
        date.month.number,
        date.day,
        date.year
    ) + this.formatTigerDine(use24Hour)
}

fun LocalDateTime.toYyyyMmDd(): String =
    "%04d-%02d-%02d".format(year, month.number, day)

fun LocalDateTime.toFDMPAPIFriendly(): String =
    "%04d/%02d/%02d".format(year, month.number, day)

fun Instant.isToday(): Boolean {
    val timeZone = TimeZone.of("America/New_York")
    return this.toLocalDateTime(timeZone).date ==
            Clock.System.now().toLocalDateTime(timeZone).date
}
