package com.example.gallery.domain.usecase

import com.example.gallery.domain.model.Album
import com.example.gallery.domain.repository.MediaRepository
import com.example.gallery.util.Result
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(): Result<List<Album>> {
        return repository.getAlbums()
    }
}