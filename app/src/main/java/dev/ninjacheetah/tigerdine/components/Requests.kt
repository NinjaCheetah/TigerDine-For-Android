package dev.ninjacheetah.tigerdine.components

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import dev.ninjacheetah.tigerdine.data.types.DiningLocationsParser
import kotlinx.serialization.json.Json

fun getAllDiningInfo(context: Context, callback: (diningData: DiningLocationsParser?) -> Unit) {
    val volleyQueue = Volley.newRequestQueue(context)
    val url = "https://tigercenter.rit.edu/tigerCenterApi/tc/dining-all?date=2026-01-26"

    val jsonObjectRequest = JsonObjectRequest(
        Request.Method.GET, url, null,
        { response ->
            println("made request")
            println("Response: %s".format(response.toString()))
            val diningData = Json.decodeFromString<DiningLocationsParser>(response.toString())
            println(diningData)
            callback(diningData)
        },
        { error ->
            println("error occurred while fetching dining data: $error")
            callback(null)
        }
    )
    volleyQueue.add(jsonObjectRequest)
}
