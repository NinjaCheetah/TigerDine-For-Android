package dev.ninjacheetah.tigerdine.data.types

import kotlinx.serialization.Serializable
import kotlin.time.Instant

// Struct to parse the response data from the TigerCenter API when getting the information for a dining location.
@Serializable
data class DiningLocationParser(
    // Other basic information to read from a location's JSON that we'll need later.
    val id: Int,
    val mdoId: Int,
    val name: String,
    val summary: String,
    val description: String,
    val mapsUrl: String,
    val department: String,
    val mapId: Int,
    val mrkId: Int,
    val catId: Int,
    val contacts: List<String>,
    val events: List<Event>,
    val menus: List<Menu>
) {
    // An individual "event", which is just an open period for the location.
    @Serializable
    data class Event(
        val id: Int,
        val name: String,
        val locationId: Int,
        val locationName: String,
        val startTime: String,
        val endTime: String,
        val startDate: String,
        val endDate: String,
        val daysMask: Int,
        val daysOfWeek: List<String>,
        val exceptions: List<HoursException>?,
        val infinite: Boolean,
        val menuTypes: List<String>?,
        val description: String?
    ) {
        // Hour exceptions for the given event.
        @Serializable
        data class HoursException(
            val id: Int,
            val name: String,
            val startTime: String,
            val endTime: String,
            val daysOfWeek: List<String>,
            val startDate: String,
            val endDate: String,
            val daysMask: Int,
            val open: Boolean,
            val infinite: Boolean
        )
    }
    // An individual "menu", which can be either a daily special item or a visiting chef. Description
    // needs to be optional because visiting chefs have descriptions but specials do not.
    @Serializable
    data class Menu(
        val id: Int,
        val name: String,
        val description: String?,
        val price: Int,
        val category: String,
        val eventId: Int
    )
}

// Struct that probably doesn't need to exist but this made parsing the list of location responses easy.
@Serializable
data class DiningLocationsParser(
    val locations: List<DiningLocationParser>
)

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
