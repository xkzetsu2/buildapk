package com.example.gallery.domain.usecase

import com.example.gallery.domain.model.MediaFilter
import com.example.gallery.domain.repository.MediaRepository
import com.example.gallery.util.Result
import javax.inject.Inject

class GetMediaCountUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(filter: MediaFilter): Result<Int> {
        return repository.getMediaCount(filter)
    }
}