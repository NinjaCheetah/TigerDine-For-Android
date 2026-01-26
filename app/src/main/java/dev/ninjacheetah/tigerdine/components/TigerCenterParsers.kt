package dev.ninjacheetah.tigerdine.components

import dev.ninjacheetah.tigerdine.data.types.DiningLocation
import dev.ninjacheetah.tigerdine.data.types.DiningLocationParser
import dev.ninjacheetah.tigerdine.data.types.DiningTimes
import dev.ninjacheetah.tigerdine.data.types.FDMPIds
import dev.ninjacheetah.tigerdine.data.types.OpenStatus
import java.util.Date

fun parseLocationInfo(location: DiningLocationParser, forDate: Date?): DiningLocation {
    println("beginning parse for ${location.name} (id: ${location.id})")

    // The descriptions sometimes have HTML <br /> tags despite also having \n.
    // Those need to be removed.
    val desc = location.description.replace("<br />", "")

    return DiningLocation(
        id = location.id,
        mdoId = location.mdoId,
        fdmpIds = null,
        name = location.name,
        summary = location.summary,
        desc = desc,
        mapsUrl = location.mapsUrl,
        date = Date(),
        diningTimes = arrayOf(
            DiningTimes(
                openTime = Date(),
                closeTime = Date()
            )
        ),
        open = OpenStatus.OPEN,
        visitingChefs = null,
        dailySpecials = null
    )
}
