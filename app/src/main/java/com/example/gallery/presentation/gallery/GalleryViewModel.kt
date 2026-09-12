package com.example.gallery.presentation.gallery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.gallery.domain.model.MediaFilter
import com.example.gallery.domain.model.MediaItem
import com.example.gallery.domain.model.SortOrder
import com.example.gallery.domain.usecase.GetMediaItemsUseCase
import com.example.gallery.util.Result
import dagger.hilt.android.scopedToActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val getMediaItemsUseCase: GetMediaItemsUseCase
) {
    private val _mediaItems = MutableStateFlow<Result<List<MediaItem>>?>(null)
    val mediaItems = _mediaItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var currentFilter: MediaFilter = MediaFilter.All()
    private var currentSort: SortOrder = SortOrder.DATE_ADDED_DESC

    fun loadMedia(filter: MediaFilter = currentFilter, sortOrder: SortOrder = currentSort) {
        currentFilter = filter
        currentSort = sortOrder
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = getMediaItemsUseCase(filter, sortOrder)
            _mediaItems.value = result
            _isLoading.value = false
            result.onFailure { e ->
                _error.value = e.message ?: "Unknown error"
            }
        }
    }

    fun refresh() {
        loadMedia(currentFilter, currentSort)
    }
}