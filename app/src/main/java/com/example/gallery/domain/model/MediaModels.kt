package com.example.gallery.domain.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey
    val id: Long,
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val width: Int,
    val height: Int,
    val size: Long,
    val dateTaken: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val relativePath: String?,
    val bucketId: String,
    val bucketDisplayName: String,
    val isFavorite: Boolean = false,
    val orientation: Int = 0,
    val duration: Long = 0
) {
    val isImage: Boolean
        get() = mimeType.startsWith("image/")

    val isVideo: Boolean
        get() = mimeType.startsWith("video/")

    val mediaUri: Uri
        get() = Uri.parse(uri)

    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height else 1f
}

@Serializable
data class Album(
    val id: String,
    val displayName: String,
    val coverUri: String?,
    val itemCount: Int,
    val dateAdded: Long,
    val dateModified: Long
) {
    val coverMediaUri: Uri?
        get() = coverUri?.let { Uri.parse(it) }
}

@Serializable
data class MediaType(
    val name: String,
    val count: Int,
    val mimePrefix: String
)

sealed interface MediaFilter {
    data class All : MediaFilter
    data class ImagesOnly : MediaFilter
    data class VideosOnly : MediaFilter
    data class FavoritesOnly : MediaFilter
    data class AlbumFilter(val albumId: String) : MediaFilter
    data class DateRange(val start: Long, val end: Long) : MediaFilter
}

enum class SortOrder {
    DATE_ADDED_DESC,
    DATE_ADDED_ASC,
    DATE_TAKEN_DESC,
    DATE_TAKEN_ASC,
    NAME_ASC,
    NAME_DESC,
    SIZE_DESC,
    SIZE_ASC
}