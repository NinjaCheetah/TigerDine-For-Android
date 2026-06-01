package dev.ninjacheetah.tigerdine.data

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import dev.ninjacheetah.tigerdine.components.getAllDiningInfo
import dev.ninjacheetah.tigerdine.components.parseLocationInfo
import dev.ninjacheetah.tigerdine.data.types.DiningLocation
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

class DiningModel(
    application: Application
) : AndroidViewModel(application) {

    var locationsByDay by mutableStateOf<List<List<DiningLocation>>>(emptyList())
        private set

    var daysRepresented by mutableStateOf<List<Instant>>(emptyList())
    var lastRefreshed by mutableStateOf<Instant?>(null)
    var isLoaded by mutableStateOf(false)

    fun getDaysRepresented() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        daysRepresented = (0..6).map { offset ->
            today
                .plus(offset, DateTimeUnit.DAY)
                .atStartOfDayIn(TimeZone.currentSystemDefault())
        }
    }

    fun getHoursByDay() {
        println("loading from network")

        getDaysRepresented()

        locationsByDay = emptyList()

        var completed = 0
        val results = MutableList<List<DiningLocation>?>(daysRepresented.size) { null }

        daysRepresented.forEachIndexed { index, day ->
            getAllDiningInfo(day, getApplication()) { parserResult ->
                if (parserResult != null) {
                    results[index] = parserResult.locations.map {
                        parseLocationInfo(it, day)
                    }
                }

                completed++

                if (completed == daysRepresented.size) {
                    locationsByDay = results.map { it ?: emptyList() }
                    lastRefreshed = Clock.System.now()
                    isLoaded = true
                }
            }
        }
    }
}
