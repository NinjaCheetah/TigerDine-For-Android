package dev.ninjacheetah.tigerdine.util

import dev.ninjacheetah.tigerdine.types.DiningLocation
import dev.ninjacheetah.tigerdine.types.DiningTimes
import dev.ninjacheetah.tigerdine.types.OpenStatus
import dev.ninjacheetah.tigerdine.types.VisitingChefStatus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlin.time.Clock
import kotlin.time.Instant

fun getOpenStatus(openTime: Instant, closeTime: Instant, referenceTime: Instant): OpenStatus {
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

fun getMultiOpenStatus(diningTimes: List<DiningTimes>?, referenceTime: Instant): OpenStatus {
    var openStatus: OpenStatus = OpenStatus.CLOSED

    if (diningTimes.isNullOrEmpty()) {
        return openStatus
    }

    for (i in diningTimes.indices) {
        openStatus = getOpenStatus(
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

// Updates the open status of a location and of its visiting chefs, so that the labels in the UI
// update automatically as time progresses and locations open/close/etc.
fun DiningLocation.withUpdatedOpenStatus(): DiningLocation {
    return copy(
        open = getMultiOpenStatus(diningTimes, Clock.System.now()),
        visitingChefs = if (!visitingChefs.isNullOrEmpty()) {
            visitingChefs.map { chef ->
                chef.copy(
                    status = when (
                        getOpenStatus(
                            chef.openTime,
                            chef.closeTime,
                            Clock.System.now()
                        )
                    ) {
                        OpenStatus.OPEN -> VisitingChefStatus.HERE_NOW
                        OpenStatus.CLOSED ->
                            if (Clock.System.now() < chef.openTime) {
                                VisitingChefStatus.ARRIVING_LATER
                            } else {
                                VisitingChefStatus.GONE
                            }

                        OpenStatus.OPENING_SOON -> VisitingChefStatus.ARRIVING_SOON
                        OpenStatus.CLOSING_SOON -> VisitingChefStatus.LEAVING_SOON
                    }
                )
            }
        } else {
            null
        }
    )
}
