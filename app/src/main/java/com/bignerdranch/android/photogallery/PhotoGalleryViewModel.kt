package com.bignerdranch.android.photogallery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.photogallery.api.GalleryItem
import com.bignerdranch.android.photogallery.api.PhotoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "PhotoGalleryViewModel"

class PhotoGalleryViewModel : ViewModel() {

    private val photoRepository = PhotoRepository()
    private val preferencesRepository = PreferencesRepository.get()
    
    private val _uiState: MutableStateFlow<PhotoGalleryUiState> = MutableStateFlow(PhotoGalleryUiState())
    val uiState: StateFlow<PhotoGalleryUiState>
    	get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch{
            preferencesRepository.storedQuery.collectLatest {
                try{
                    val listItems = getGalleryItems(it)
                    _uiState.update { oldState ->
                        oldState.copy(
                            images = listItems,
                            query = it
                        )
                    }
                }
                catch(ex: Exception){
                    Log.e(TAG, "Failed to fetch gallery items", ex)
                }
            }
        }
    }

    fun setQuery(query: String){
        viewModelScope.launch{
            preferencesRepository.setStoredQuery(query)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

    private suspend fun getGalleryItems(query: String): List<GalleryItem>{
        return if(query.isNotBlank()){
            photoRepository.searchPhotos(query)
        }
        else{
            photoRepository.fetchPhotos()
        }
    }

    data class PhotoGalleryUiState(
        val images: List<GalleryItem> = emptyList(),
        val query: String = ""
    )

}