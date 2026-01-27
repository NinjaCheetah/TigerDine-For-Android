package dev.ninjacheetah.tigerdine.data

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import dev.ninjacheetah.tigerdine.components.getAllDiningInfo
import dev.ninjacheetah.tigerdine.components.parseLocationInfo
import dev.ninjacheetah.tigerdine.data.types.DiningLocation
import kotlin.time.Clock

class DiningModel(
    application: Application
) : AndroidViewModel(application) {

    var diningData by mutableStateOf<List<DiningLocation>>(emptyList())
        private set

    fun getHoursByDay() {
        getAllDiningInfo(getApplication()) { parserResult ->
            if (parserResult != null) {
                diningData = parserResult.locations.map {
                    parseLocationInfo(it, Clock.System.now())
                }
            }
        }
    }
}
