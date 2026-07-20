package dev.ninjacheetah.tigerdine.data.persistent

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiningCacheRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val LAST_REFRESHED_DATE =
            longPreferencesKey("last_refreshed_date")

        private val CACHED_DINING_DATA =
            stringPreferencesKey("cached_dining_data")
    }

    val lastRefreshedDate: Flow<Long?> =
        dataStore.data.map {
            it[LAST_REFRESHED_DATE]
        }

    val cachedDiningData: Flow<String?> =
        dataStore.data.map {
            it[CACHED_DINING_DATA]
        }

    suspend fun updateDiningCache(json: String, timestamp: Long) {
        dataStore.edit {
            it[CACHED_DINING_DATA] = json
            it[LAST_REFRESHED_DATE] = timestamp
        }
    }
}

val Context.diningCacheDataStore by preferencesDataStore(
    name = "dining_cache"
)
