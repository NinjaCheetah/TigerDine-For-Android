package dev.ninjacheetah.tigerdine.data

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import dev.ninjacheetah.tigerdine.util.toYyyyMmDd
import dev.ninjacheetah.tigerdine.data.types.DiningLocationsParser
import dev.ninjacheetah.tigerdine.data.types.FDMealPeriodsParser
import dev.ninjacheetah.tigerdine.data.types.FDMealsParser
import dev.ninjacheetah.tigerdine.util.toFDMPAPIFriendly
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
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

            val url = "https://tigercenter.rit.edu/tigerCenterApi/tc/dining-all?date=${targetDate.toYyyyMmDd()}"
            //val url = "https://tigercenter.rit.edu/tigerCenterApi/tc/dining-all?date=2026-04-23"

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

    suspend fun getFDMealPlannerOpenings(
        locationId: Int
    ): FDMealPeriodsParser? =
        suspendCancellableCoroutine { continuation ->
            val url = "https://apiservicelocatorstenantrit.fdmealplanner.com/api/v1/data-locator-webapi/20/mealPeriods?LocationId=${locationId}"

            println("making openings request to: $url")

            val request = JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                { response ->
                    continuation.resume(
                        Json {
                            ignoreUnknownKeys = true
                        }.decodeFromString<FDMealPeriodsParser>(
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

    suspend fun getFDMealPlannerMenu(
        locationId: Int,
        accountId: Int,
        mealPeriodId: Int
    ): FDMealsParser? =
        suspendCancellableCoroutine { continuation ->
            val zone = TimeZone.currentSystemDefault()
            val date = Clock.System.now().toLocalDateTime(zone)
            val dateString = date.toFDMPAPIFriendly()

            val url = "https://apiservicelocatorstenantrit.fdmealplanner.com/api/v1/data-locator-webapi" +
                    "/20/meals?menuId=0&accountId=${accountId}&locationId=${locationId}&mealPeriodId=${mealPeriodId}" +
                    "&tenantId=20&monthId=${date.month.number}&startDate=${dateString}&endDate=${dateString}"
//            val url = "https://apiservicelocatorstenantrit.fdmealplanner.com/api/v1/data-locator-webapi" +
//                    "/20/meals?menuId=0&accountId=${accountId}&locationId=${locationId}&mealPeriodId=${mealPeriodId}" +
//                    "&tenantId=20&monthId=4&startDate=2026/04/15&endDate=2026/04/15"

            println("making menu request to: $url")

            val request = JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                { response ->
                    continuation.resume(
                        Json {
                            ignoreUnknownKeys = true
                        }.decodeFromString<FDMealsParser>(
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
