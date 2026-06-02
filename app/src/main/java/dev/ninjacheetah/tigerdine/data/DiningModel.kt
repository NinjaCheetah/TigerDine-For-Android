package dev.ninjacheetah.tigerdine.data

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.ninjacheetah.tigerdine.components.getAllDiningInfo
import dev.ninjacheetah.tigerdine.components.parseLocationInfo
import dev.ninjacheetah.tigerdine.data.types.DiningLocation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

// TODO: migrate from AndroidViewModel to just ViewModel because it's apparently overkill for my
// use case, since I shouldn't need the application here.
class DiningModel(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    var locationsByDay by mutableStateOf<List<List<DiningLocation>>>(emptyList())
        private set

    var daysRepresented by mutableStateOf<List<Instant>>(emptyList())
    var lastRefreshed by mutableStateOf<Instant?>(null)
    var isLoaded by mutableStateOf(false)
    var isRefreshing by mutableStateOf(false)

    val openLocationsOnly =
        settingsRepository.openLocationsOnly
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                false
            )

    fun setOpenLocationsOnly(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setOpenLocationsOnly(enabled)
        }
    }

    val openLocationsFirst =
        settingsRepository.openLocationsFirst
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                false
            )

    fun setOpenLocationsFirst(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setOpenLocationsFirst(enabled)
        }
    }

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

        isRefreshing = true

        getDaysRepresented()

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
                    isRefreshing = false
                }
            }
        }
    }
}

class DiningModelFactory(
    private val application: Application,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiningModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiningModel(application, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
