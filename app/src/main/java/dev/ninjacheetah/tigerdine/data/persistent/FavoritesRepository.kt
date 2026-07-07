package dev.ninjacheetah.tigerdine.data.persistent

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val FAVORITE_LOCATIONS = stringSetPreferencesKey("favorite_locations")
    }

    val favoriteLocations: Flow<Set<Int>> = dataStore.data.map { preferences ->
        preferences[FAVORITE_LOCATIONS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    suspend fun toggleFavorite(locationId: Int) {
        dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_LOCATIONS] ?: emptySet()
            val idString = locationId.toString()
            
            if (currentFavorites.contains(idString)) {
                preferences[FAVORITE_LOCATIONS] = currentFavorites - idString
            } else {
                preferences[FAVORITE_LOCATIONS] = currentFavorites + idString
            }
        }
    }
}

val Context.favoritesDataStore by preferencesDataStore(
    name = "favorites"
)
