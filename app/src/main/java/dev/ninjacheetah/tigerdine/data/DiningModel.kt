package dev.ninjacheetah.tigerdine.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

class DiningModel(
    private val diningRepository: DiningRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

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
        viewModelScope.launch {
            isRefreshing = true

            try {
                getDaysRepresented()

                val results = mutableListOf<List<DiningLocation>>()

                for (day in daysRepresented) {
                    val parserResult =
                        diningRepository.getAllDiningInfo(day)

                    results += parserResult
                        ?.locations
                        ?.map { parseLocationInfo(it, day) }
                        ?: emptyList()
                }

                locationsByDay = results
                lastRefreshed = Clock.System.now()
                isLoaded = true
            } finally {
                isRefreshing = false
            }
        }
    }
}

class DiningModelFactory(
    private val diningRepository: DiningRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiningModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiningModel(diningRepository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
