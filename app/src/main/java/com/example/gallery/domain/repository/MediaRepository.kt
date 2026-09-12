package com.example.gallery.domain.repository

import com.example.gallery.domain.model.Album
import com.example.gallery.domain.model.MediaFilter
import com.example.gallery.domain.model.MediaItem
import com.example.gallery.domain.model.MediaType
import com.example.gallery.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun getMediaItems(
        filter: MediaFilter,
        sortOrder: SortOrder,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): Result<List<MediaItem>>

    fun observeMediaItems(
        filter: MediaFilter,
        sortOrder: SortOrder
    ): Flow<Result<List<MediaItem>>>

    suspend fun getMediaItem(id: Long): Result<MediaItem>

    suspend fun getAlbums(): Result<List<Album>>

    fun observeAlbums(): Flow<Result<List<Album>>>

    suspend fun getMediaTypes(): Result<List<MediaType>>

    suspend fun toggleFavorite(id: Long): Result<Boolean>

    suspend fun deleteMediaItems(ids: List<Long>): Result<Int>

    suspend fun scanMedia(): Result<Int>

    suspend fun getMediaCount(filter: MediaFilter): Result<Int>
}