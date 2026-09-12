package com.example.gallery.presentation.gallery

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gallery.domain.model.MediaFilter
import com.example.gallery.domain.model.MediaItem
import com.example.gallery.domain.model.SortOrder
import com.example.gallery.ui.theme.GalleryTheme
import com.example.gallery.util.Result
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun GalleryScreen(
    onItemClick: (MediaItem) -> Unit,
    onAlbumClick: (String) -> Unit
) {
    val viewModel: GalleryViewModel = hiltViewModel()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val filter by remember { mutableStateOf<MediaFilter>(MediaFilter.All()) }
    val sortOrder by remember { mutableStateOf<SortOrder>(SortOrder.DATE_ADDED_DESC) }
    val showFilterDialog by remember { mutableStateOf(false) }
    val showSortDialog by remember { mutableStateOf(false) }

    val mediaItems = viewModel.mediaItems.collectAsStateWithLifecycle().value
    val isLoading = viewModel.isLoading.value
    val error = viewModel.error.value

    GalleryTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 0.dp)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Gallery",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* TODO: Open drawer */ }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        IconButton(onClick = { /* TODO: Search */ }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )

                if (isLoading && mediaItems.isNullOrEmpty()) {
                    LoadingIndicator()
                } else if (error != null) {
                    ErrorView(error = error, onRetry = { viewModel.loadMedia(filter, sortOrder) })
                } else {
                    MediaGrid(
                        mediaItems = mediaItems ?: emptyList(),
                        onItemClick = onItemClick,
                        spanCount = 3
                    )
                }
            }

            if (showFilterDialog) {
                FilterDialog(
                    currentFilter = filter,
                    onFilterSelected = { newFilter ->
                        filter = newFilter
                        viewModel.loadMedia(newFilter, sortOrder)
                        showFilterDialog = false
                    },
                    onDismiss = { showFilterDialog = false }
                )
            }

            if (showSortDialog) {
                SortDialog(
                    currentSort = sortOrder,
                    onSortSelected = { newSort ->
                        sortOrder = newSort
                        viewModel.loadMedia(filter, newSort)
                        showSortDialog = false
                    },
                    onDismiss = { showSortDialog = false }
                )
            }
        }
    }
}

@Composable
fun MediaGrid(
    mediaItems: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
    spanCount: Int = 3
) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(spanCount),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(mediaItems) { item ->
            MediaGridItem(
                mediaItem = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
fun MediaGridItem(
    mediaItem: MediaItem,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .clip(androidx.compose.ui.graphics.RectangleShape)
    ) {
        if (mediaItem.isImage) {
            coil.compose.AsyncImage(
                model = mediaItem.mediaUri,
                contentDescription = mediaItem.displayName,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = androidx.compose.foundation.background(
                    Modifier.fillMaxSize(),
                    MaterialTheme.colorScheme.surfaceVariant
                )
            )
        } else {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                coil.compose.AsyncImage(
                    model = mediaItem.mediaUri,
                    contentDescription = mediaItem.displayName,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.PlayCircleFilled,
                    contentDescription = "Video",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.size(48.dp)
                )
                if (mediaItem.duration > 0) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .align(Alignment.BottomStart),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            text = formatDuration(mediaItem.duration),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (mediaItem.isFavorite) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopEnd)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(
    error: String,
    onRetry: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Failed to load media",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = error,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            androidx.compose.material3.Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun FilterDialog(
    currentFilter: MediaFilter,
    onFilterSelected: (MediaFilter) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter", fontWeight = FontWeight.Medium) },
        text = {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    MediaFilter.All() to "All Media",
                    MediaFilter.ImagesOnly() to "Images Only",
                    MediaFilter.VideosOnly() to "Videos Only",
                    MediaFilter.FavoritesOnly() to "Favorites"
                ).forEach { (filter, label) ->
                    androidx.compose.material3.RadioButton(
                        selected = currentFilter == filter,
                        onClick = { onFilterSelected(filter) },
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SortDialog(
    currentSort: SortOrder,
    onSortSelected: (SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort By", fontWeight = FontWeight.Medium) },
        text = {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOrder.values().forEach { sort ->
                    androidx.compose.material3.RadioButton(
                        selected = currentSort == sort,
                        onClick = { onSortSelected(sort) },
                        label = { Text(sort.displayName) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun SortOrder.displayName: String = when (this) {
    SortOrder.DATE_ADDED_DESC -> "Date Added (Newest)"
    SortOrder.DATE_ADDED_ASC -> "Date Added (Oldest)"
    SortOrder.DATE_TAKEN_DESC -> "Date Taken (Newest)"
    SortOrder.DATE_TAKEN_ASC -> "Date Taken (Oldest)"
    SortOrder.NAME_ASC -> "Name (A-Z)"
    SortOrder.NAME_DESC -> "Name (Z-A)"
    SortOrder.SIZE_DESC -> "Size (Largest)"
    SortOrder.SIZE_ASC -> "Size (Smallest)"
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes >= 60) {
        val hours = minutes / 60
        val mins = minutes % 60
        "%d:%02d:%02d".format(hours, mins, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

@Composable
fun AlbumGridItem(
    album: com.example.gallery.domain.model.Album,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .clip(androidx.compose.ui.graphics.RectangleShape)
    ) {
        album.coverMediaUri?.let { uri ->
            coil.compose.AsyncImage(
                model = uri,
                contentDescription = album.displayName,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = androidx.compose.foundation.background(
                    Modifier.fillMaxSize(),
                    MaterialTheme.colorScheme.surfaceVariant
                )
            )
        } ?: androidx.compose.foundation.background(
            Modifier.fillMaxSize(),
            MaterialTheme.colorScheme.surfaceVariant
        )

        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(12.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            androidx.compose.foundation.layout.Column {
                Text(
                    text = album.displayName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "${album.itemCount} items",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}