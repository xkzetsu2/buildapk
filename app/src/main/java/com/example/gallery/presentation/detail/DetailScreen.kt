package com.example.gallery.presentation.detail

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.zoomable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import coil.transform.Transformation
import com.example.gallery.domain.model.MediaItem
import com.example.gallery.ui.theme.GalleryTheme
import com.example.gallery.util.Result
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun DetailScreen(
    mediaItem: MediaItem,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onInfo: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val viewModel: DetailViewModel = hiltViewModel()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showAppBar by remember { mutableStateOf(true) }
    var showBottomBar by remember { mutableStateOf(true) }

    GalleryTheme {
        Scaffold(
            topBar = {
                if (showAppBar) {
                    TopAppBar(
                        title = {
                            Text(
                                text = mediaItem.displayName,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onClose) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = onInfo) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                            }
                            IconButton(onClick = onShare) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                            }
                            IconButton(onClick = onToggleFavorite) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isFavorite) "Remove from favorites" : "Add to favorites",
                                    tint = if (isFavorite) Color.Red else Color.White
                                )
                            }
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Black.copy(alpha = 0.7f)
                        )
                    )
                }
            },
            bottomBar = {
                if (showBottomBar && mediaItem.isVideo) {
                    VideoControls(
                        mediaItem = mediaItem,
                        onPlayPause = { /* TODO */ },
                        onSeek = { /* TODO */ }
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showAppBar = !showAppBar; showBottomBar = !showBottomBar },
                        onDoubleTap = { /* TODO: Zoom in/out */ }
                    )
                }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .pointerInput(Unit) {
                        zoomable(
                            minScale = 1f,
                            maxScale = 5f,
                            onZoomStarted = { /* TODO */ },
                            onZoom = { newScale ->
                                scale = newScale.coerceIn(1f..5f)
                            },
                            onZoomStopped = { /* TODO */ }
                        )
                    }
            ) {
                if (mediaItem.isImage) {
                    ZoomableImage(
                        uri = mediaItem.mediaUri,
                        scale = scale,
                        offset = offset,
                        contentDescription = mediaItem.displayName
                    )
                } else {
                    VideoPlayer(
                        uri = mediaItem.mediaUri,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(
    uri: android.net.Uri,
    scale: Float,
    offset: Offset,
    contentDescription: String
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
            }
            .clip(androidx.compose.ui.graphics.RectangleShape)
    )
}

@Composable
fun VideoPlayer(
    uri: android.net.Uri,
    modifier: Modifier = Modifier
) {
    androidx.media3.ui.PlayerView(LocalContext.current).also { playerView ->
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { playerView },
            modifier = modifier.fillMaxSize(),
            update = { view ->
                val player = androidx.media3.exoplayer.ExoPlayer.Builder(LocalContext.current).build()
                player.setMediaItem(androidx.media3.common.MediaItem.fromUri(uri.toString()))
                player.prepare()
                player.playWhenReady = true
                view.player = player
            }
        )
    }
}

@Composable
fun VideoControls(
    mediaItem: MediaItem,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "0:00",
                color = Color.White,
                fontSize = 12.sp
            )
            IconButton(onClick = onPlayPause) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Text(
                text = formatDuration(mediaItem.duration),
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun InfoDialog(
    mediaItem: MediaItem,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Details", fontWeight = FontWeight.Medium) },
        text = {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow("Name", mediaItem.displayName)
                DetailRow("Type", if (mediaItem.isImage) "Image" else "Video")
                DetailRow("Dimensions", "${mediaItem.width} x ${mediaItem.height}")
                DetailRow("Size", formatFileSize(mediaItem.size))
                DetailRow("Date Taken", formatDate(mediaItem.dateTaken))
                DetailRow("Date Added", formatDate(mediaItem.dateAdded))
                DetailRow("Album", mediaItem.bucketDisplayName)
                if (mediaItem.duration > 0) {
                    DetailRow("Duration", formatDuration(mediaItem.duration))
                }
                if (mediaItem.orientation != 0) {
                    DetailRow("Orientation", mediaItem.orientation.toString())
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(text = value, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
        else -> "%.1f GB".format(bytes / (1024.0 * 1024 * 1024))
    }
}

fun formatDate(timestamp: Long): String {
    return if (timestamp > 0) {
        java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    } else {
        "Unknown"
    }
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
fun LocalContext.current(): android.content.Context = androidx.compose.ui.platform.LocalContext.current