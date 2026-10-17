package dev.ninjacheetah.tigerdine.types

import kotlinx.serialization.Serializable

// Struct to parse the response data from the TigerCenter API when getting the information for a dining location.
@Serializable
data class DiningLocationTCParser(
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
data class DiningLocationsTCParser(
    val locations: List<DiningLocationTCParser>
)
