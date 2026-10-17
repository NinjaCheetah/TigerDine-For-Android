package dev.ninjacheetah.tigerdine.data.parsers

import dev.ninjacheetah.tigerdine.data.constant.constLocationInfo
import dev.ninjacheetah.tigerdine.data.constant.tCtoFDMPMap
import dev.ninjacheetah.tigerdine.types.DailySpecial
import dev.ninjacheetah.tigerdine.types.DiningLocation
import dev.ninjacheetah.tigerdine.types.DiningLocationRMParser
import dev.ninjacheetah.tigerdine.types.DiningTimes
import dev.ninjacheetah.tigerdine.types.FDMPIds
import dev.ninjacheetah.tigerdine.types.FoodTruck
import dev.ninjacheetah.tigerdine.types.FoodTruckAvailability
import dev.ninjacheetah.tigerdine.types.FoodTruckRMParser
import dev.ninjacheetah.tigerdine.types.OpenStatus
import dev.ninjacheetah.tigerdine.types.VisitingChef
import dev.ninjacheetah.tigerdine.types.VisitingChefStatus
import dev.ninjacheetah.tigerdine.util.getInstantFromTimeStr
import dev.ninjacheetah.tigerdine.util.getOpenStatus
import dev.ninjacheetah.tigerdine.util.toYyyyMmDd
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

fun parseLocationInfoRM(location: DiningLocationRMParser, forDate: Instant?): DiningLocation {
    println("beginning parse for ${location.name} (id: ${location.id}) from RM API")
    println(forDate)

    // Pull the matching summary from the hardcoded map.
    val summary: String = if (constLocationInfo.keys.contains(location.id)) {
        constLocationInfo.getValue(location.id).getValue("summary")
    } else {
        location.summary
    }

    // Pull the matching description from the hardcoded map.
    val desc: String = if (constLocationInfo.keys.contains(location.id)) {
        constLocationInfo.getValue(location.id).getValue("description")
    } else {
        location.description
    }

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

    // Generate a maps URL from the mdoId key. Unlike TigerCenter, the RIT Mobile API only returns
    // the bare map ID instead of also including a full link.
    val mapsUrl = "https://maps.rit.edu/?mdo_id=${location.mdoId}"

    // Get the YYYY-MM-DD date string for the day we're processing.
    val zone = TimeZone.currentSystemDefault()
    val dateStr: String = forDate?.let {
        forDate.toLocalDateTime(zone).toYyyyMmDd()
    } ?: run {
        Clock.System.now().toLocalDateTime(zone).toYyyyMmDd()
    }

    // Early return if there are no hours scheduled.
    if (!location.openHours.keys.contains(dateStr)) {
        return DiningLocation(
            id = location.id,
            mdoId = location.mdoId,
            fdmpIds = fdmpIds,
            name = location.name,
            summary = summary,
            desc = desc,
            mapsUrl = mapsUrl,
            websiteUrl = location.url,
            date = forDate ?: Clock.System.now(),
            diningTimes = null,
            open = OpenStatus.CLOSED,
            visitingChefs = null,
            dailySpecials = null
        )
    }

    val openStrings = mutableListOf<String>()
    val closeStrings = mutableListOf<String>()

    // Unlike TigerCenter, the RIT Mobile API always lists the hours for each day individually, so
    // we don't have to worry about all the nonsense that we had to worry about for TigerCenter.
    if (location.openHours.contains(dateStr)) {
        for ((startTime, endTime, isOpen) in location.openHours.getValue(dateStr)) {
            if (isOpen) {
                openStrings.add(startTime)
                closeStrings.add(endTime)
            }
        }
    }

    // I actually don't think this early return is possible for the RIT Mobile API like it was for
    // TigerCenter, but I'd rather be safe than sorry. Generally there are just no entry and not
    // an empty entry if the location is closed.
    if (openStrings.isEmpty() || closeStrings.isEmpty()) {
        return DiningLocation(
            id = location.id,
            mdoId = location.mdoId,
            fdmpIds = fdmpIds,
            name = location.name,
            summary = summary,
            desc = desc,
            mapsUrl = mapsUrl,
            websiteUrl = location.url,
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
    // ^I forgot about this comment but yeah wow this code isn't quite as bad as iOS. Which is nice
    // since the uglier block isn't required for the RIT Mobile APIs so the worst parsing code on
    // Android really isn't even that bad :3
    val openTimes = mutableListOf<Instant>()
    val closeTimes = mutableListOf<Instant>()

    for (i in 0..<openStrings.count()) {
        openTimes.add(getInstantFromTimeStr(openStrings[i], forDate))
        closeTimes.add(getInstantFromTimeStr(closeStrings[i], forDate))
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

    // Sometimes the openings are not in order, for some reason. I know this happened with
    // TigerCenter, and I'd like to think it won't happen with the RIT Mobile APIs, but I'd rather
    // be safe than sorry.
    diningTimes.sortWith(compareBy { it.openTime })

    // Get the current open status for a location. Details about how this works can be seen in
    // the docs for parseOpenStatus().
    var openStatus: OpenStatus = OpenStatus.CLOSED
    for (i in diningTimes.indices) {
        openStatus =
            getOpenStatus(diningTimes[i].openTime, diningTimes[i].closeTime, Clock.System.now())
        // If the first event pass came back closed, loop again in case a later event has a
        // different status. This is mostly to accurately catch Gracie's multiple open periods
        // each day.
        if (openStatus != OpenStatus.CLOSED) {
            break
        }
    }

    // Handle visiting chefs, which can be done so much more easily than with the TigerCenter APIs.
    val visitingChefs: List<VisitingChef>?
    if (location.visitingChefs.keys.contains(dateStr)) {
        val chefs = mutableListOf<VisitingChef>()
        for (chef in location.visitingChefs.getValue(dateStr)) {
            println("found visiting chef: ${chef.name}")
            val openTime = getInstantFromTimeStr(chef.startTime, forDate)
            val closeTime = getInstantFromTimeStr(chef.endTime, forDate)

            // Parse the chef's status, mapping the OpenStatus to a VisitingChefStatus.
            val visitingChefStatus: VisitingChefStatus = when (getOpenStatus(
                openTime,
                closeTime,
                Clock.System.now()
            )) {
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
                    chef.name,
                    chef.description.ifBlank {
                        "No description available."
                    }, // Sometimes visiting chefs don't have descriptions
                    openTime,
                    closeTime,
                    visitingChefStatus
                )
            )
        }
        visitingChefs = chefs
    } else {
        visitingChefs = null
    }

    // Same with the daily specials.
    val dailySpecials: List<DailySpecial>?
    if (location.dailySpecials.isNotEmpty()) {
        // I need RIT to actually put in some daily specials so that I can understand how to parse
        // this data!!!!! Until then, we're just going to always default to null.
        dailySpecials = null
    } else {
        dailySpecials = null
    }

    return DiningLocation(
        id = location.id,
        mdoId = location.mdoId,
        fdmpIds = fdmpIds,
        name = location.name,
        summary = summary,
        desc = desc,
        mapsUrl = mapsUrl,
        websiteUrl = location.url,
        date = forDate ?: Clock.System.now(),
        diningTimes = diningTimes,
        open = openStatus,
        visitingChefs = visitingChefs,
        dailySpecials = dailySpecials
    )
}

fun parseFoodTruckInfoRM(truck: FoodTruckRMParser): FoodTruck {
    val availability = mutableListOf<FoodTruckAvailability>()

    for (dateAvailable in truck.datesAvailable) {
        // Get today's date to use for the availability and to combine with the timestamps.
        val ldt = LocalDateTime.parse("${dateAvailable.date}T00:00:00")
        val date = ldt.toInstant(TimeZone.of("America/New_York"))

        val openTime = getInstantFromTimeStr(dateAvailable.startTime, date)
        val closeTime = getInstantFromTimeStr(dateAvailable.endTime, date)

        availability.add(
            FoodTruckAvailability(
                date = date,
                openTime = openTime,
                closeTime = closeTime,
                mapsUrl = dateAvailable.url,
                locationName = dateAvailable.urlTitle,
                location = dateAvailable.location
            )
        )
    }

    return FoodTruck(
        id = truck.id.toInt(), // This **SHOULD** always be okay??
        name = truck. title,
        description = truck.description,
        imageUrl = truck.imageUrl,
        imageAltTag = truck.imageAltTag,
        url = truck.url,
        availability = availability
    )
}
