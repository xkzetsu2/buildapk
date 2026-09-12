package com.example.gallery.di

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Dimension
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    @Provides
    @Singleton
    fun provideImageLoader(context: Context): ImageLoader {
        return ImageLoader(context).apply {
            this.defaultRequestOptions = listOf(
                ImageRequest.Options()
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
            )
        }
    }
}