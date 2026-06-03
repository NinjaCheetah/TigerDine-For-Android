package dev.ninjacheetah.tigerdine.data

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import dev.ninjacheetah.tigerdine.components.toYyyyMmDd
import dev.ninjacheetah.tigerdine.data.types.DiningLocationsParser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.time.Clock
import kotlin.time.Instant

class DiningRepository(
    private val requestQueue: RequestQueue
) {
    suspend fun getAllDiningInfo(
        date: Instant?
    ): DiningLocationsParser? =
        suspendCancellableCoroutine { continuation ->

            val zone = TimeZone.currentSystemDefault()
            val targetDate =
                date?.toLocalDateTime(zone)
                    ?: Clock.System.now().toLocalDateTime(zone)

            val url =
                "https://tigercenter.rit.edu/tigerCenterApi/tc/dining-all?date=${targetDate.toYyyyMmDd()}"

            val request = JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                { response ->
                    continuation.resume(
                        Json.decodeFromString<DiningLocationsParser>(
                            response.toString()
                        )
                    )
                },
                { error ->
                    println("error occurred while fetching dining data: $error")
                    continuation.resume(null)
                }
            )

            requestQueue.add(request)
        }
}
