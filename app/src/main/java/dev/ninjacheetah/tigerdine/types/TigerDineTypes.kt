package dev.ninjacheetah.tigerdine.types

import kotlinx.serialization.Serializable
import kotlin.time.Instant

// Enum to represent the four possible states a given location can be in.
@Serializable
enum class OpenStatus {
    OPEN, CLOSED, OPENING_SOON, CLOSING_SOON
}

// An individual open period for a location.
@Serializable
data class DiningTimes(
    var openTime: Instant,
    var closeTime: Instant
)

// Enum to represent the five possible states a visiting chef can be in.
@Serializable
enum class VisitingChefStatus {
    HERE_NOW, GONE, ARRIVING_LATER, ARRIVING_SOON, LEAVING_SOON
}

// A visiting chef present at a location.
@Serializable
data class VisitingChef(
    val name: String,
    val description: String,
    var openTime: Instant,
    var closeTime: Instant,
    val status: VisitingChefStatus
)

// A daily special at a location.
@Serializable
data class DailySpecial(
    val name: String,
    val type: String
)

// The IDs required to get the menu for a location from FD MealPlanner. Only present if the location appears in the map.
@Serializable
data class FDMPIds(
    val locationId: Int,
    val accountId: Int
)

// The basic information about a dining location needed to display it in the app after parsing is finished.
@Serializable
data class DiningLocation(
    val id: Int,
    val mdoId: Int,
    val fdmpIds: FDMPIds?,
    val name: String,
    val summary: String,
    val desc: String,
    val mapsUrl: String,
    val websiteUrl: String?,
    val date: Instant,
    val diningTimes: List<DiningTimes>?,
    val open: OpenStatus,
    val visitingChefs: List<VisitingChef>?,
    val dailySpecials: List<DailySpecial>?
)

// Parser to read the occupancy data for a location.
@Serializable
data class DiningOccupancyParser(
    val count: Int,
    val location: String,
    val building: String,
    val mdo_id: Int,
    val max_occ: Int,
    val open_status: String,
    val intra_loc_hours: List<HourlyOccupancy>
) {
    // Represents a per-hour occupancy rating.
    @Serializable
    data class HourlyOccupancy(
        val hour: Int,
        val today: Int,
        val today_max: Int,
        val one_week_ago: Int,
        val one_week_ago_max: Int,
        val average: Int
    )
}

// Struct used to represent a day and its hours as strings. Type used for the hours of today and the next 6 days used in DetailView.
data class WeeklyHours(
    val day: String,
    val date: Instant,
    val timeStrings: List<String>
)
