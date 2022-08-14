package com.bignerdranch.android.photogallery

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PreferencesRepository private constructor(
    private val dataStore: DataStore<Preferences>,
    private val coroutineScope: CoroutineScope = GlobalScope
) {

    val storedQuery: Flow<String> = dataStore.data.map {
    	it[SEARCH_QUERY_KEY] ?: ""
    }.distinctUntilChanged()
    
    suspend fun setStoredQuery(query: String) {
        dataStore.edit {
    		it[SEARCH_QUERY_KEY] = query
    	}
    }

    companion object {
        private var INSTANCE: PreferencesRepository? = null
        private val SEARCH_QUERY_KEY = stringPreferencesKey("search_query")

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                val dataStore = PreferenceDataStoreFactory.create {
                    context.preferencesDataStoreFile("settings")
                }

                INSTANCE = PreferencesRepository(dataStore)
            }
        }

        fun get(): PreferencesRepository {
            return INSTANCE
                ?: throw IllegalStateException("PreferencesRepository must be initialized")
        }
    }
}