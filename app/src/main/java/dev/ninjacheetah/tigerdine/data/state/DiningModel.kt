package dev.ninjacheetah.tigerdine.data.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.ninjacheetah.tigerdine.data.DiningRepository
import dev.ninjacheetah.tigerdine.data.SettingsRepository
import dev.ninjacheetah.tigerdine.data.constant.tCtoFDMPMap
import dev.ninjacheetah.tigerdine.util.parseLocationInfo
import dev.ninjacheetah.tigerdine.data.types.DiningLocation
import dev.ninjacheetah.tigerdine.data.types.FDMenuItem
import dev.ninjacheetah.tigerdine.util.isToday
import dev.ninjacheetah.tigerdine.util.parseFDMealPlannerMenu
import dev.ninjacheetah.tigerdine.util.withUpdatedOpenStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class DiningModel(
    private val diningRepository: DiningRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // ------------------------------------------------------------------------
    // Init stuff
    //
    //      Anything that needs to happen when the view model is first created.
    // ------------------------------------------------------------------------

    init {
        startUpdateOpenStatuesTimer()
    }


    // Start an always running timer to automatically update the open statues for all the locations,
    // so that the status labels change automatically as time progresses. This will also
    // automatically reload the dining data if the last refreshed date is no longer today.
    private fun startUpdateOpenStatuesTimer() {
        viewModelScope.launch {
            while (isActive) {
                delay(3.seconds)

                updateOpenStatuses()
                if (!(lastRefreshed?.isToday() ?: false)) {
                    getHoursByDay()
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Main Dining Information Section
    //
    //      Holds the key stuff like the locations themselves and their hours.
    //      Any data found in this section is sourced from the TigerCenter
    //      API.
    // ------------------------------------------------------------------------

    var locationsByDay by mutableStateOf<List<List<DiningLocation>>>(emptyList())
        private set

    var daysRepresented by mutableStateOf<List<Instant>>(emptyList())
    var lastRefreshed by mutableStateOf<Instant?>(null)
    var isLoaded by mutableStateOf(false)
    var isRefreshing by mutableStateOf(false)
    var focusedLocationId by mutableIntStateOf(0)

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

    // Helper function that gets run by LaunchedEffect. This makes sure that we only run the
    // actual getHoursByDay() if we haven't already done so, because it would be silly to reload
    // the data every single time you navigate back the home screen.
    fun getHoursByDayIfNeeded() {
        if (locationsByDay.isEmpty()) {
            getHoursByDay()
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

    fun updateOpenStatuses() {
        locationsByDay = locationsByDay.map { day ->
            day.map { location ->
                location.withUpdatedOpenStatus()
            }
        }
    }

    // ------------------------------------------------------------------------
    // FDMealPlanner Menus Section
    //
    //      Holds data like valid meal periods and the current menu for a
    //      given location. May or may not be populated depending on the
    //      current state of the app.
    // ------------------------------------------------------------------------

    var haveMenuForLocationId by mutableIntStateOf(0)
    var openPeriods: List<Int> by mutableStateOf(emptyList())
    var selectedMealPeriod by mutableIntStateOf(0)
    var menuItems: List<FDMenuItem> by mutableStateOf(emptyList())
    var menuIsLoaded by mutableStateOf(false)
    var menuIsLoading by mutableStateOf(true)

    fun getOpenPeriods() {
        if (openPeriods.isEmpty() || haveMenuForLocationId != focusedLocationId) {
            viewModelScope.launch {
                val openingsParser = diningRepository.getFDMealPlannerOpenings(tCtoFDMPMap[focusedLocationId]!!.first)

                if (openingsParser != null) {
                    openPeriods = openingsParser.data.map { it.id }
                    println(openingsParser.data)
                    println(openPeriods)
                    selectedMealPeriod = openPeriods.first()
                    getMenuForPeriod()
                }
            }
        }
    }

    fun getMenuForPeriod() {
        viewModelScope.launch {
            menuIsLoaded = false
            menuIsLoading = true

            val rawMenuItems = diningRepository.getFDMealPlannerMenu(
                locationId = tCtoFDMPMap[focusedLocationId]!!.first,
                accountId = tCtoFDMPMap[focusedLocationId]!!.second,
                mealPeriodId = selectedMealPeriod
            )

            if (rawMenuItems != null) {
                menuItems = parseFDMealPlannerMenu(rawMenuItems)
                haveMenuForLocationId = focusedLocationId
                menuIsLoading = false
                menuIsLoaded = true
            }
        }
    }

    fun changeSelectedMealPeriod(newSelectedPeriod: Int) {
        selectedMealPeriod = newSelectedPeriod
        getMenuForPeriod()
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
