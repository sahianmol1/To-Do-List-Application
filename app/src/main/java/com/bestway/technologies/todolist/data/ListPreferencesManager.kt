package com.bestway.technologies.todolist.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private const val TAG = "ListPreferencesManager"
data class ListFilterPreferences(val sortOrder: SortOrder)

class ListPreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.createDataStore("list_preferences")

    val preferencesFlow = dataStore.data
            .catch { exception ->
                if(exception is IOException) {
                    Log.e(TAG, exception.toString() )
                }else {
                    throw exception
                }
            }.map { preferences ->
                return@map SortOrder.valueOf(
                        preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name
                )
            }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }


    private object PreferencesKeys {
        val SORT_ORDER = preferencesKey<String>("sort_order")
    }


}