package dev.ninjacheetah.tigerdine.data.persistent

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DietaryRestrictionsRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val NO_BEEF =
            booleanPreferencesKey("no_beef")

        private val NO_PORK =
            booleanPreferencesKey("no_pork")

        private val VEGETARIAN =
            booleanPreferencesKey("vegetarian")

        private val VEGAN =
            booleanPreferencesKey("vegan")

        private val ACTIVE_ALLERGENS = stringSetPreferencesKey("active_allergens")
    }

    val noBeef: Flow<Boolean> =
        dataStore.data.map {
            it[NO_BEEF] ?: false
        }

    suspend fun setNoBeef(value: Boolean) {
        dataStore.edit {
            it[NO_BEEF] = value
        }
    }

    val noPork: Flow<Boolean> =
        dataStore.data.map {
            it[NO_PORK] ?: false
        }

    suspend fun setNoPork(value: Boolean) {
        dataStore.edit {
            it[NO_PORK] = value
        }
    }

    val vegetarian: Flow<Boolean> =
        dataStore.data.map {
            it[VEGETARIAN] ?: false
        }

    suspend fun setVegetarian(value: Boolean) {
        dataStore.edit {
            it[VEGETARIAN] = value
        }
    }

    val vegan: Flow<Boolean> =
        dataStore.data.map {
            it[VEGAN] ?: false
        }

    suspend fun setVegan(value: Boolean) {
        dataStore.edit {
            it[VEGAN] = value
        }
    }

    val activeAllergens: Flow<Set<String>> =
        dataStore.data.map {
            it[ACTIVE_ALLERGENS] ?: emptySet()
        }

    suspend fun toggleActiveAllergen(allergen: String) {
        dataStore.edit { preferences ->
            println("toggling allergen: $allergen, current allergens: ${preferences[ACTIVE_ALLERGENS]}")

            val currentAllergens = preferences[ACTIVE_ALLERGENS] ?: emptySet()

            if (currentAllergens.contains(allergen)) {
                preferences[ACTIVE_ALLERGENS] = currentAllergens - allergen
            } else {
                preferences[ACTIVE_ALLERGENS] = currentAllergens + allergen
            }
        }
    }
}

val Context.dietaryRestrictionsDataStore by preferencesDataStore(
    name = "dietary_restrictions"
)
