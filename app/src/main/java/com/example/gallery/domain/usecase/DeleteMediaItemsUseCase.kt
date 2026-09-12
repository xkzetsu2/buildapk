package com.example.gallery.domain.usecase

import com.example.gallery.domain.repository.MediaRepository
import com.example.gallery.util.Result
import javax.inject.Inject

class DeleteMediaItemsUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(ids: List<Long>): Result<Int> {
        return repository.deleteMediaItems(ids)
    }
}