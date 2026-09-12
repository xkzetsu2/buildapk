package com.example.gallery.data.source.local

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.gallery.domain.model.Album
import com.example.gallery.domain.model.MediaFilter
import com.example.gallery.domain.model.MediaItem
import com.example.gallery.domain.model.MediaType
import com.example.gallery.domain.model.SortOrder
import com.example.gallery.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class MediaStoreDataSource @Inject constructor(
    private val context: Context
) {
    private val contentResolver: ContentResolver = context.contentResolver
    private val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.WIDTH,
        MediaStore.MediaColumns.HEIGHT,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.DATE_TAKEN,
        MediaStore.MediaColumns.DATE_ADDED,
        MediaStore.MediaColumns.DATE_MODIFIED,
        MediaStore.MediaColumns.RELATIVE_PATH,
        MediaStore.MediaColumns.BUCKET_ID,
        MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
        MediaStore.MediaColumns.IS_FAVORITE,
        MediaStore.Images.ImageColumns.ORIENTATION,
        MediaStore.Video.VideoColumns.DURATION
    )

    private val albumCache = ConcurrentHashMap<String, Album>()
    private val typeCache = ConcurrentHashMap<String, MediaType>()

    suspend fun getMediaItems(
        filter: MediaFilter,
        sortOrder: SortOrder,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0
    ): Result<List<MediaItem>> = withContext(Dispatchers.IO) {
        try {
            val selection = buildSelection(filter)
            val sortOrderClause = buildSortOrder(sortOrder)
            val uri = buildContentUri(filter)

            contentResolver.query(
                uri,
                projection,
                selection,
                null,
                "$sortOrderClause LIMIT $limit OFFSET $offset"
            )?.use { cursor ->
                Result.success(cursor.map { cursorToMediaItem(it) }.toList())
            } ?: Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMediaItem(id: Long): Result<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    Result.success(cursorToMediaItem(cursor))
                } else {
                    Result.failure(IllegalArgumentException("Media not found: $id"))
                }
            } ?: Result.failure(IllegalArgumentException("Media not found: $id"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAlbums(): Result<List<Album>> = withContext(Dispatchers.IO) {
        try {
            val uri = MediaStore.Files.getContentUri("external")
            val albumProjection = arrayOf(
                MediaStore.MediaColumns.BUCKET_ID,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns._ID
            )

            val selection = "${MediaStore.MediaColumns.MIME_TYPE} LIKE 'image/%' OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE 'video/%'"
            val groupBy = "${MediaStore.MediaColumns.BUCKET_ID}, ${MediaStore.MediaColumns.BUCKET_DISPLAY_NAME}"

            contentResolver.query(
                uri,
                albumProjection,
                selection,
                null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val albums = mutableListOf<Album>()
                val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_ID)
                val bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                val dateModifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)

                while (cursor.moveToNext()) {
                    val bucketId = cursor.getString(bucketIdIndex) ?: ""
                    val bucketName = cursor.getString(bucketNameIndex) ?: "Unknown"
                    val dateAdded = cursor.getLong(dateAddedIndex)
                    val dateModified = cursor.getLong(dateModifiedIndex)
                    val coverId = cursor.getLong(idIndex)

                    val coverUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), coverId).toString()

                    val count = getAlbumItemCount(bucketId)

                    albums.add(Album(
                        id = bucketId,
                        displayName = bucketName,
                        coverUri = coverUri,
                        itemCount = count,
                        dateAdded = dateAdded,
                        dateModified = dateModified
                    ))
                }
                Result.success(albums)
            } ?: Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMediaTypes(): Result<List<MediaType>> = withContext(Dispatchers.IO) {
        try {
            val uri = MediaStore.Files.getContentUri("external")
            val selection = "${MediaStore.MediaColumns.MIME_TYPE} LIKE 'image/%' OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE 'video/%'"

            contentResolver.query(
                uri,
                arrayOf(MediaStore.MediaColumns.MIME_TYPE),
                selection,
                null,
                null
            )?.use { cursor ->
                val mimeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val counts = mutableMapOf<String, Int>()

                while (cursor.moveToNext()) {
                    val mime = cursor.getString(mimeIndex) ?: ""
                    val prefix = if (mime.startsWith("image/")) "image/" else "video/"
                    counts[prefix] = counts.getOrDefault(prefix, 0) + 1
                }

                val types = counts.map { (prefix, count) ->
                    MediaType(
                        name = if (prefix == "image/") "Images" else "Videos",
                        count = count,
                        mimePrefix = prefix
                    )
                }
                Result.success(types)
            } ?: Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(id: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
            val current = getMediaItem(id).getOrNull()?.isFavorite ?: false
            val values = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.IS_FAVORITE, !current)
            }
            val updated = contentResolver.update(uri, values, null, null)
            Result.success(updated > 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMediaItems(ids: List<Long>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var deleted = 0
            for (id in ids) {
                val uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                val result = contentResolver.delete(uri, null, null)
                if (result > 0) deleted++
            }
            Result.success(deleted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scanMedia(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val scanner = android.media.MediaScannerConnection(context)
            val directories = arrayOf(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES).absolutePath,
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM).absolutePath,
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MOVIES).absolutePath
            )
            for (dir in directories) {
                scanner.scanFile(dir, null)
            }
            Result.success(directories.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMediaCount(filter: MediaFilter): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val selection = buildSelection(filter)
            val uri = buildContentUri(filter)

            contentResolver.query(
                uri,
                arrayOf("COUNT(*)"),
                selection,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    Result.success(cursor.getInt(0))
                } else {
                    Result.success(0)
                }
            } ?: Result.success(0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildSelection(filter: MediaFilter): String? {
        val conditions = mutableListOf<String>()
        conditions.add("(${MediaStore.MediaColumns.MIME_TYPE} LIKE 'image/%' OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE 'video/%')")

        when (filter) {
            is MediaFilter.ImagesOnly -> conditions.add("${MediaStore.MediaColumns.MIME_TYPE} LIKE 'image/%'")
            is MediaFilter.VideosOnly -> conditions.add("${MediaStore.MediaColumns.MIME_TYPE} LIKE 'video/%'")
            is MediaFilter.FavoritesOnly -> conditions.add("${MediaStore.MediaColumns.IS_FAVORITE} = 1")
            is MediaFilter.AlbumFilter -> conditions.add("${MediaStore.MediaColumns.BUCKET_ID} = '${filter.albumId}'")
            is MediaFilter.DateRange -> {
                conditions.add("${MediaStore.MediaColumns.DATE_TAKEN} >= ${filter.start}")
                conditions.add("${MediaStore.MediaColumns.DATE_TAKEN} <= ${filter.end}")
            }
            is MediaFilter.All -> {}
        }

        return conditions.joinToString(" AND ")
    }

    private fun buildSortOrder(sortOrder: SortOrder): String {
        return when (sortOrder) {
            SortOrder.DATE_ADDED_DESC -> "${MediaStore.MediaColumns.DATE_ADDED} DESC"
            SortOrder.DATE_ADDED_ASC -> "${MediaStore.MediaColumns.DATE_ADDED} ASC"
            SortOrder.DATE_TAKEN_DESC -> "${MediaStore.MediaColumns.DATE_TAKEN} DESC"
            SortOrder.DATE_TAKEN_ASC -> "${MediaStore.MediaColumns.DATE_TAKEN} ASC"
            SortOrder.NAME_ASC -> "${MediaStore.MediaColumns.DISPLAY_NAME} ASC"
            SortOrder.NAME_DESC -> "${MediaStore.MediaColumns.DISPLAY_NAME} DESC"
            SortOrder.SIZE_DESC -> "${MediaStore.MediaColumns.SIZE} DESC"
            SortOrder.SIZE_ASC -> "${MediaStore.MediaColumns.SIZE} ASC"
        }
    }

    private fun buildContentUri(filter: MediaFilter): Uri {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else -> MediaStore.Files.getContentUri("external")
        }
    }

    private fun cursorToMediaItem(cursor: Cursor): MediaItem {
        val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
        val displayNameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
        val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
        val widthIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
        val heightIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
        val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
        val dateTakenIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
        val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
        val dateModifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
        val relativePathIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
        val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_ID)
        val bucketDisplayNameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
        val isFavoriteIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.IS_FAVORITE)
        val orientationIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION)
        val durationIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)

        val uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), cursor.getLong(idIndex))

        return MediaItem(
            id = cursor.getLong(idIndex),
            uri = uri.toString(),
            displayName = cursor.getString(displayNameIndex) ?: "Unknown",
            mimeType = cursor.getString(mimeTypeIndex) ?: "application/octet-stream",
            width = cursor.getInt(widthIndex),
            height = cursor.getInt(heightIndex),
            size = cursor.getLong(sizeIndex),
            dateTaken = cursor.getLong(dateTakenIndex),
            dateAdded = cursor.getLong(dateAddedIndex),
            dateModified = cursor.getLong(dateModifiedIndex),
            relativePath = cursor.getString(relativePathIndex),
            bucketId = cursor.getString(bucketIdIndex) ?: "",
            bucketDisplayName = cursor.getString(bucketDisplayNameIndex) ?: "Unknown",
            isFavorite = cursor.getInt(isFavoriteIndex) == 1,
            orientation = if (orientationIndex >= 0) cursor.getInt(orientationIndex) else 0,
            duration = if (durationIndex >= 0) cursor.getLong(durationIndex) else 0L
        )
    }

    private fun getAlbumItemCount(bucketId: String): Int {
        val uri = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.MediaColumns.BUCKET_ID} = ? AND (${MediaStore.MediaColumns.MIME_TYPE} LIKE 'image/%' OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE 'video/%')"
        return contentResolver.query(uri, arrayOf("COUNT(*)"), selection, arrayOf(bucketId), null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        } ?: 0
    }
}