package com.miral.unsplash.domain.photo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.miral.unsplash.data.photo.PhotoService
import com.miral.unsplash.data.photo.model.Photo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(private val photoService: PhotoService) {

    fun getPhotos(): Flow<PagingData<Photo>> =
        Pager(
            config = PagingConfig(
                initialLoadSize = 15,
                pageSize = 15,
                maxSize = 100,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PhotoPagingSource(photoService) }
        ).flow

    fun getTopicPhotos(topicId: String): Flow<PagingData<Photo>> =
        Pager(
            config = PagingConfig(
                initialLoadSize = 15,
                pageSize = 15,
                maxSize = 100,
                prefetchDistance = 5,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { TopicPhotoPagingSource(photoService, topicId) }
        ).flow

    suspend fun getPhotoDetails(id: String) = photoService.getPhoto(id)
}