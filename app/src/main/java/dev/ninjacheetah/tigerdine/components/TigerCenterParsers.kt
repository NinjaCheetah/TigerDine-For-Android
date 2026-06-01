package dev.ninjacheetah.tigerdine.components

import dev.ninjacheetah.tigerdine.data.constant.tCtoFDMPMap
import dev.ninjacheetah.tigerdine.data.types.DailySpecial
import dev.ninjacheetah.tigerdine.data.types.DiningLocation
import dev.ninjacheetah.tigerdine.data.types.DiningLocationParser
import dev.ninjacheetah.tigerdine.data.types.DiningTimes
import dev.ninjacheetah.tigerdine.data.types.FDMPIds
import dev.ninjacheetah.tigerdine.data.types.OpenStatus
import dev.ninjacheetah.tigerdine.data.types.VisitingChef
import dev.ninjacheetah.tigerdine.data.types.VisitingChefStatus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

fun parseOpenStatus(openTime: Instant, closeTime: Instant, referenceTime: Instant): OpenStatus {
    return when {
        referenceTime in openTime..closeTime -> {
            val openNextDay = openTime.plus(24, DateTimeUnit.HOUR)
            if (closeTime == openNextDay) {
                OpenStatus.OPEN
            } else if (closeTime <= referenceTime.plus(30, DateTimeUnit.MINUTE)) {
                OpenStatus.CLOSING_SOON
            } else {
                OpenStatus.OPEN
            }
        }
        referenceTime < openTime && openTime <= referenceTime.plus(30, DateTimeUnit.MINUTE) -> {
            OpenStatus.OPENING_SOON
        }
        else -> OpenStatus.CLOSED
    }
}

fun parseMultiOpenStatus(diningTimes: List<DiningTimes>?, referenceTime: Instant): OpenStatus {
    var openStatus: OpenStatus = OpenStatus.CLOSED

    if (diningTimes.isNullOrEmpty()) {
        return openStatus
    }

    for (i in diningTimes.indices) {
        openStatus = parseOpenStatus(
            diningTimes[i].openTime,
            diningTimes[i].closeTime,
            referenceTime
        )
        // If the first event pass came back closed, loop again in case a later event has a
        // different status. This is mostly to accurately catch Gracie's/Brick City Cafe's multiple
        // open periods each day.
        if (openStatus != OpenStatus.CLOSED) {
            return openStatus
        }
    }

    return OpenStatus.CLOSED
}

fun parseLocationInfo(location: DiningLocationParser, forDate: Instant?): DiningLocation {
    println("beginning parse for ${location.name} (id: ${location.id})")
    println(forDate)

    // The descriptions sometimes have HTML <br /> tags despite also having \n.
    // Those need to be removed.
    val desc = location.description.replace("<br />", "")

    // Check if this location's ID is in the TigerCenter -> FD MealPlanner ID map and
    // save those IDs if it is.
    val fdmpIds: FDMPIds? = (if (tCtoFDMPMap.keys.contains(location.id)) {
        val (locationId, accountId) = tCtoFDMPMap[location.id]!!
        FDMPIds(
            locationId = locationId,
            accountId = accountId
        )
    } else {
        null
    })

    // Generate a maps URL from the mdoId key. This is required because the mapsUrl served by
    // TigerCenter is not compatible with the new RIT map that was deployed in December 2025.
    val mapsUrl = "https://maps.rit.edu/?mdo_id=${location.mdoId}"

    // Early return if there are no events, good for things like the food trucks which can
    // very easily have no openings in a week.
    if (location.events.isEmpty()) {
        return DiningLocation(
            id = location.id,
            mdoId = location.mdoId,
            fdmpIds = fdmpIds,
            name = location.name,
            summary = location.summary,
            desc = desc,
            mapsUrl = mapsUrl,
            date = forDate ?: Clock.System.now(),
            diningTimes = null,
            open = OpenStatus.CLOSED,
            visitingChefs = null,
            dailySpecials = null
        )
    }

    val openStrings = mutableListOf<String>()
    val closeStrings = mutableListOf<String>()

    // Dining locations have a regular schedule, but then they also have exceptions listed for days
    // like weekends or holidays. If there are exceptions, use those times for the day, otherwise
    // we can just use the default times. Also check for repeats! The response data can include
    // those sometimes, for reasons:tm:
    // This gets the current day of the weeks as an all-caps string.
    val dayStr = (forDate ?: Clock.System.now())
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .dayOfWeek
        .name
    for (event in location.events) {
        if (!event.exceptions.isNullOrEmpty() &&
            // This additional check is necessary, because sometimes the exceptions are silly and
            // are doing something like marking a location as closed on a day that isn't included
            // in the regular schedule anyway. That breaks things. This check ensures that the
            // exception being looked at applies for the day we're parsing for before trying to
            // follow it.
            dayStr in event.exceptions[0].daysOfWeek)
        {
            // Only save the exception times if the location is actually open during those times,
            // and if these times aren't a repeat. I've seen repeats for Brick City Cafe
            // specifically, where both the breakfast and lunch standard open periods had exceptions
            // listing the same singular brunch period. That feels like a stupid choice but oh well.
            if (event.exceptions[0].open &&
                !openStrings.contains(event.exceptions[0].startTime) &&
                !closeStrings.contains(event.exceptions[0].endTime))
            {
                openStrings.add(event.exceptions[0].startTime)
                closeStrings.add(event.exceptions[0].endTime)
            }
        } else {
            if (!openStrings.contains(event.startTime) && !closeStrings.contains(event.endTime)) {
                // Verify that the current weekday falls within the schedule. The regular event
                // schedule specifies which days of the week it applies to, and if the current
                // day isn't in that list and there are no exceptions, that means there are no
                // hours for this location.
                if (dayStr in event.daysOfWeek) {
                    openStrings.add(event.startTime)
                    closeStrings.add(event.endTime)
                }
            }
        }
    }

    // Early return if there are no valid opening times, most likely because the day's exceptions
    // dictate that the location is closed. Mostly comes into play on holidays.
    if (openStrings.isEmpty() || closeStrings.isEmpty()) {
        return DiningLocation(
            id = location.id,
            mdoId = location.mdoId,
            fdmpIds = fdmpIds,
            name = location.name,
            summary = location.summary,
            desc = desc,
            mapsUrl = mapsUrl,
            date = forDate ?: Clock.System.now(),
            diningTimes = null,
            open = OpenStatus.CLOSED,
            visitingChefs = null,
            dailySpecials = null
        )
    }

    // I actually DON'T hate this time parsing code as much as I hate the iOS code. Which I think
    // just means that I should go back and clean it up because it has to be possible to make it
    // cleaner than it is right now.
    val openTimes = mutableListOf<Instant>()
    val closeTimes = mutableListOf<Instant>()

    for (i in 0..<openStrings.count()) {
        val (openHour, openMinute, openSecond) = openStrings[i].split(":").map { it.toInt() }
        val (closeHour, closeMinute, closeSecond) = closeStrings[i].split(":").map { it.toInt() }

        val zone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(zone).date

        openTimes.add(
            LocalDateTime(
                today.year,
                today.month,
                today.day,
                openHour,
                openMinute,
                openSecond
            ).toInstant(zone)
        )

        closeTimes.add(
            LocalDateTime(
                today.year,
                today.month,
                today.day,
                closeHour,
                closeMinute,
                closeSecond
            ).toInstant(zone)
        )
    }
    // Save the parsed dining times for the location.
    val diningTimes = mutableListOf<DiningTimes>()
    for (i in 0..<openTimes.count()) {
        diningTimes.add(
            DiningTimes(
                openTimes[i],
                closeTimes[i]
            )
        )
    }

    // If the closing time is less than or equal to the opening time, it's probably midnight and
    // means either open until midnight or open 24/7, in the case of Bytes.
    for (i in diningTimes.indices) {
        if (diningTimes[i].closeTime <= diningTimes[i].openTime) {
            diningTimes[i].closeTime = diningTimes[i].closeTime.plus(24, DateTimeUnit.HOUR)
        }
    }

    // Sometimes the openings are not in order, for some reason. I'm observing this with Brick City,
    // where for some reason the early opening is event 1, and the later opening is event 0.
    // This is silly so let's reverse it.
    diningTimes.sortWith(compareBy { it.openTime })

    // Get the current open status for a location. Details about how this works can be seen in
    // the docs for parseOpenStatus().
    var openStatus: OpenStatus = OpenStatus.CLOSED
    for (i in diningTimes.indices) {
        openStatus = parseOpenStatus(diningTimes[i].openTime, diningTimes[i].closeTime, Clock.System.now())
        // If the first event pass came back closed, loop again in case a later event has a
        // different status. This is mostly to accurately catch Gracie's multiple open periods
        // each day.
        if (openStatus != OpenStatus.CLOSED) {
            break
        }
    }

    val visitingChefs: List<VisitingChef>?
    val dailySpecials: List<DailySpecial>?
    if (!location.menus.isEmpty()) {
        val chefs = mutableListOf<VisitingChef>()
        val specials = mutableListOf<DailySpecial>()

        val zone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(zone)

        for (menu in location.menus) {
            if (menu.category == "Visiting Chef") {
                var name: String = menu.name
                val splitString = name.split("(", limit = 2)
                name = splitString[0].trim()
                // Time parsing nonsense starts here. Extracts the time from a string like
                // "Chef (4-7p.m.)", splits it at the "-", strips the non-numerical characters
                // from each part, parses it as a number and adds 12 hours as needed, then creates
                // a Date instance for that time on today's date.
                val timeStrings = splitString[1].replace(")", "").split("-", limit = 2)
                println("raw open range: $timeStrings")

                val openTime: Instant
                val closeTime: Instant

                if (timeStrings.count() == 2) {
                    // If the time is NOT in the morning, add 12 hours.
                    val openHour: Int = if (timeStrings.first().trim().contains("a") && timeStrings.first().trim().contains("m")) {
                        timeStrings.first().trim().filter { it.isDigit() }.toInt()
                    } else {
                        try {
                            timeStrings.first().trim().toInt() + 12
                        } catch (_: NumberFormatException) {
                            12
                        }

                    }
                    openTime = LocalDateTime(
                        today.year,
                        today.month,
                        today.day,
                        openHour,
                        0,
                        0
                    ).toInstant(zone)

                    // I've chosen to assume that no visiting chef will ever close in the morning.
                    // This could be a bad choice, but I have yet to see any evidence of a visiting
                    // chef leaving before noon so far.
                    val closeHour = timeStrings.last().trim().filter { it.isDigit() }.toInt() + 12
                    closeTime = LocalDateTime(
                        today.year,
                        today.month,
                        today.day,
                        closeHour,
                        0,
                        0
                    ).toInstant(zone)
                } else {
                    break
                }

                // Parse the chef's status, mapping the OpenStatus to a VisitingChefStatus.
                val visitngChefStatus: VisitingChefStatus = when (parseOpenStatus(openTime, closeTime, Clock.System.now())) {
                    OpenStatus.OPEN -> VisitingChefStatus.HERE_NOW
                    OpenStatus.CLOSED -> {
                        if (Clock.System.now() < openTime) {
                            VisitingChefStatus.ARRIVING_LATER
                        } else {
                            VisitingChefStatus.GONE
                        }
                    }
                    OpenStatus.OPENING_SOON -> VisitingChefStatus.ARRIVING_SOON
                    OpenStatus.CLOSING_SOON -> VisitingChefStatus.LEAVING_SOON
                }

                chefs.add(
                    VisitingChef(
                        name,
                        menu.description ?: "No description available", // Sometimes visiting chefs don't have descriptions
                        openTime,
                        closeTime,
                        visitngChefStatus
                    )
                )
            } else if (menu.category == "Daily Specials") {
                println("found daily special: ${menu.name}")
                val splitString = menu.name.split("(", limit = 2)
                specials.add(
                    DailySpecial(
                        splitString[0],
                        (if (splitString.count() > 1) splitString[1] else "").replace(")", "")
                    )
                )
            }
        }
        visitingChefs = chefs
        dailySpecials = specials
    } else {
        visitingChefs = null
        dailySpecials = null
    }

    return DiningLocation(
        id = location.id,
        mdoId = location.mdoId,
        fdmpIds = fdmpIds,
        name = location.name,
        summary = location.summary,
        desc = desc,
        mapsUrl = mapsUrl,
        date = forDate ?: Clock.System.now(),
        diningTimes = diningTimes,
        open = openStatus,
        visitingChefs = visitingChefs,
        dailySpecials = dailySpecials
    )
}

// Updates the open status of a location and of its visiting chefs, so that the labels in the UI
// update automatically as time progresses and locations open/close/etc.
fun DiningLocation.updateOpenStatus() {
    this.open = parseMultiOpenStatus(diningTimes, Clock.System.now())
    if (!this.visitingChefs.isNullOrEmpty()) {
        for (i in visitingChefs.indices) {
            this.visitingChefs[i].status = when (parseOpenStatus(visitingChefs[i].openTime, visitingChefs[i].closeTime,
                Clock.System.now())) {
                OpenStatus.OPEN -> VisitingChefStatus.HERE_NOW
                OpenStatus.CLOSED -> {
                    if (Clock.System.now() < visitingChefs[i].openTime) {
                        VisitingChefStatus.ARRIVING_LATER
                    } else {
                        VisitingChefStatus.GONE
                    }
                }
                OpenStatus.OPENING_SOON -> VisitingChefStatus.ARRIVING_SOON
                OpenStatus.CLOSING_SOON -> VisitingChefStatus.LEAVING_SOON
            }
        }
    }
}
