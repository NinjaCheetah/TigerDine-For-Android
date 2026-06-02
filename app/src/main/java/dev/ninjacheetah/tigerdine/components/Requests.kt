package dev.ninjacheetah.tigerdine.components

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import dev.ninjacheetah.tigerdine.data.types.DiningLocationsParser
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

fun getAllDiningInfo(date: Instant?, context: Context, callback: (diningData: DiningLocationsParser?) -> Unit) {
    val zone = TimeZone.currentSystemDefault()
    val targetDate = date?.toLocalDateTime(zone) ?: Clock.System.now().toLocalDateTime(zone)
    val url = "https://tigercenter.rit.edu/tigerCenterApi/tc/dining-all?date=${targetDate.toYyyyMmDd()}"

    val jsonObjectRequest = JsonObjectRequest(
        Request.Method.GET, url, null,
        { response ->
            val diningData = Json.decodeFromString<DiningLocationsParser>(response.toString())
            callback(diningData)
        },
        { error ->
            println("error occurred while fetching dining data: $error")
            callback(null)
        }
    )

    // Create volley request queue and add the request to it.
    val volleyQueue = Volley.newRequestQueue(context.applicationContext)
    volleyQueue.add(jsonObjectRequest)
}
