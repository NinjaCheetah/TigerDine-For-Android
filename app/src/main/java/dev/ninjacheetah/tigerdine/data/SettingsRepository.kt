package dev.ninjacheetah.tigerdine.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val OPEN_LOCATIONS_ONLY =
            booleanPreferencesKey("open_locations_only")

        private val OPEN_LOCATIONS_FIRST =
            booleanPreferencesKey("open_locations_first")
    }

    val openLocationsOnly: Flow<Boolean> =
        dataStore.data.map {
            it[OPEN_LOCATIONS_ONLY] ?: false
        }

    val openLocationsFirst: Flow<Boolean> =
        dataStore.data.map {
            it[OPEN_LOCATIONS_FIRST] ?: false
        }

    suspend fun setOpenLocationsOnly(value: Boolean) {
        dataStore.edit {
            it[OPEN_LOCATIONS_ONLY] = value
        }
    }

    suspend fun setOpenLocationsFirst(value: Boolean) {
        dataStore.edit {
            it[OPEN_LOCATIONS_FIRST] = value
        }
    }
}

val Context.dataStore by preferencesDataStore(
    name = "settings"
)
