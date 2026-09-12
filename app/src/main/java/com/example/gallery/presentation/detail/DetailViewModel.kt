package com.example.gallery.presentation.detail

import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.gallery.domain.model.MediaItem
import com.example.gallery.domain.usecase.ToggleFavoriteUseCase
import com.example.gallery.util.Result
import dagger.hilt.android.scopedToActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) {
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    fun setMediaItem(item: MediaItem) {
        _isFavorite.value = item.isFavorite
    }

    fun toggleFavorite(item: MediaItem) {
        viewModelScope.launch {
            val result = toggleFavoriteUseCase(item.id)
            result.onSuccess { success ->
                if (success) {
                    _isFavorite.value = !_isFavorite.value
                }
            }
        }
    }
}