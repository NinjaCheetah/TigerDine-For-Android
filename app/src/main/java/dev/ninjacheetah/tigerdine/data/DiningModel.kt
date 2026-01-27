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

    val sortedDiningData: List<DiningLocation>
        get() {
            fun removeThe(name: String) =
                if (name.startsWith("The ", ignoreCase = true)) name.drop(4) else name

            return diningData.sortedWith { a, b ->
                removeThe(a.name)
                    .compareTo(removeThe(b.name), ignoreCase = true)
            }
        }

    val locationsWithChefs: List<DiningLocation>
        get() = diningData.filter { location ->
            !location.visitingChefs.isNullOrEmpty()
        }

    fun getHoursByDay() {
        getAllDiningInfo(null, getApplication()) { parserResult ->
            if (parserResult != null) {
                diningData = parserResult.locations.map {
                    parseLocationInfo(it, Clock.System.now())
                }
            }
        }
    }
}
