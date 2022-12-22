package com.miral.unsplash.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.miral.unsplash.data.photo.PhotoService
import com.miral.unsplash.data.topic.TopicService
import com.miral.unsplash.utils.Network
import com.miral.unsplash.utils.NetworkConnectivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val BASE_URL = "https://api.unsplash.com/"
@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun providePhotoService(retrofit: Retrofit): PhotoService =
        retrofit.create(PhotoService::class.java)

    @Provides
    @Singleton
    fun provideTopicService(retrofit: Retrofit): TopicService =
        retrofit.create(TopicService::class.java)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Singleton
    @Provides
    fun provideNetworkConnectivity(@ApplicationContext context: Context) : NetworkConnectivity {
        return Network(context)
    }
}