package com.example.gallery.domain.usecase

import com.example.gallery.domain.model.MediaFilter
import com.example.gallery.domain.model.MediaItem
import com.example.gallery.domain.model.SortOrder
import com.example.gallery.domain.repository.MediaRepository
import com.example.gallery.util.Result
import javax.inject.Inject

class GetMediaItemsUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(
        filter: MediaFilter = MediaFilter.All(),
        sortOrder: SortOrder = SortOrder.DATE_ADDED_DESC,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): Result<List<MediaItem>> {
        return repository.getMediaItems(filter, sortOrder, limit, offset)
    }
}