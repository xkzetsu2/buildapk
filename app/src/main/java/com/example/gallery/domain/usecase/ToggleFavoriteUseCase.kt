package com.example.gallery.domain.usecase

import com.example.gallery.domain.repository.MediaRepository
import com.example.gallery.util.Result
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(id: Long): Result<Boolean> {
        return repository.toggleFavorite(id)
    }
}