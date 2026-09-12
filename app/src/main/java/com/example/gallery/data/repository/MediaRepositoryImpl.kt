package com.example.gallery.data.repository

import com.example.gallery.data.source.local.MediaStoreDataSource
import com.example.gallery.domain.model.Album
import com.example.gallery.domain.model.MediaFilter
import com.example.gallery.domain.model.MediaItem
import com.example.gallery.domain.model.MediaType
import com.example.gallery.domain.model.SortOrder
import com.example.gallery.domain.repository.MediaRepository
import com.example.gallery.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val dataSource: MediaStoreDataSource
) : MediaRepository {

    override suspend fun getMediaItems(
        filter: MediaFilter,
        sortOrder: SortOrder,
        limit: Int,
        offset: Int
    ): Result<List<MediaItem>> {
        return dataSource.getMediaItems(filter, sortOrder, limit, offset)
    }

    override fun observeMediaItems(
        filter: MediaFilter,
        sortOrder: SortOrder
    ): Flow<Result<List<MediaItem>>> {
        return flow {
            while (true) {
                val result = dataSource.getMediaItems(filter, sortOrder)
                emit(result)
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }

    override suspend fun getMediaItem(id: Long): Result<MediaItem> {
        return dataSource.getMediaItem(id)
    }

    override suspend fun getAlbums(): Result<List<Album>> {
        return dataSource.getAlbums()
    }

    override fun observeAlbums(): Flow<Result<List<Album>>> {
        return flow {
            while (true) {
                val result = dataSource.getAlbums()
                emit(result)
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }

    override suspend fun getMediaTypes(): Result<List<MediaType>> {
        return dataSource.getMediaTypes()
    }

    override suspend fun toggleFavorite(id: Long): Result<Boolean> {
        return dataSource.toggleFavorite(id)
    }

    override suspend fun deleteMediaItems(ids: List<Long>): Result<Int> {
        return dataSource.deleteMediaItems(ids)
    }

    override suspend fun scanMedia(): Result<Int> {
        return dataSource.scanMedia()
    }

    override suspend fun getMediaCount(filter: MediaFilter): Result<Int> {
        return dataSource.getMediaCount(filter)
    }
}