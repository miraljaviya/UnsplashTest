package com.miral.unsplash.domain.topic

import com.miral.unsplash.data.topic.TopicService
import com.miral.unsplash.data.topic.model.Topic
import com.miral.unsplash.utils.NetworkConnectivity
import com.miral.unsplash.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicRepository @Inject constructor(
    private val topicService: TopicService,
    private val networkConnectivity: NetworkConnectivity,) {

    suspend fun getTopics(per_page: Int = 15): Resource<List<Topic>> {
        if (!networkConnectivity.isConnected()) {
            return Resource.Error("No internet connection")
        }

        return try {
            val response = topicService.getTopics(per_page)
            val result = response.body()
            if (response.isSuccessful && result != null) {
                Resource.Success(result)
            } else {
                Resource.Error(response.message())
            }
        } catch(e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}