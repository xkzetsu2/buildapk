package com.example.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gallery.domain.model.MediaItem
import com.example.gallery.presentation.detail.DetailScreen
import com.example.gallery.presentation.gallery.GalleryScreen
import com.example.gallery.ui.theme.GalleryTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GalleryTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val json = Json { ignoreUnknownKeys = true }
                    NavHost(navController, startDestination = "gallery") {
                        composable("gallery") {
                            GalleryScreen(
                                onItemClick = { item ->
                                    val jsonString = json.encodeToString(item)
                                    navController.navigate("detail?item=$jsonString")
                                },
                                onAlbumClick = { albumId ->
                                    // TODO: Navigate to album detail
                                }
                            )
                        }
                        composable(
                            route = "detail?item={item}",
                            arguments = listOf(androidx.navigation.navArgument("item") { type = androidx.navigation.NavType.StringType })
                        ) { backStackEntry ->
                            val itemJson = backStackEntry.getString()?.getString("item") ?: ""
                            val item = json.decodeFromString<MediaItem>(itemJson)
                            DetailScreen(
                                mediaItem = item,
                                onClose = { navController.popBackStack() },
                                onDelete = { /* TODO */ },
                                onShare = { /* TODO */ },
                                onInfo = { /* TODO */ },
                                onToggleFavorite = { /* TODO */ }
                            )
                        }
                    }
                }
            }
        }
    }
}