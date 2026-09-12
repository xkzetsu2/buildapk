package com.example.gallery.di

import com.example.gallery.data.repository.MediaRepositoryImpl
import com.example.gallery.data.source.local.MediaStoreDataSource
import com.example.gallery.domain.repository.MediaRepository
import com.example.gallery.domain.usecase.DeleteMediaItemsUseCase
import com.example.gallery.domain.usecase.GetAlbumsUseCase
import com.example.gallery.domain.usecase.GetMediaCountUseCase
import com.example.gallery.domain.usecase.GetMediaItemsUseCase
import com.example.gallery.domain.usecase.GetMediaTypesUseCase
import com.example.gallery.domain.usecase.ScanMediaUseCase
import com.example.gallery.domain.usecase.ToggleFavoriteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMediaRepository(impl: MediaRepositoryImpl): MediaRepository = impl

    @Provides
    @Singleton
    fun provideMediaStoreDataSource(dataSource: MediaStoreDataSource): MediaStoreDataSource = dataSource
}

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetMediaItemsUseCase(repository: MediaRepository): GetMediaItemsUseCase =
        GetMediaItemsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetAlbumsUseCase(repository: MediaRepository): GetAlbumsUseCase =
        GetAlbumsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetMediaTypesUseCase(repository: MediaRepository): GetMediaTypesUseCase =
        GetMediaTypesUseCase(repository)

    @Provides
    @Singleton
    fun provideToggleFavoriteUseCase(repository: MediaRepository): ToggleFavoriteUseCase =
        ToggleFavoriteUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteMediaItemsUseCase(repository: MediaRepository): DeleteMediaItemsUseCase =
        DeleteMediaItemsUseCase(repository)

    @Provides
    @Singleton
    fun provideScanMediaUseCase(repository: MediaRepository): ScanMediaUseCase =
        ScanMediaUseCase(repository)

    @Provides
    @Singleton
    fun provideGetMediaCountUseCase(repository: MediaRepository): GetMediaCountUseCase =
        GetMediaCountUseCase(repository)
}