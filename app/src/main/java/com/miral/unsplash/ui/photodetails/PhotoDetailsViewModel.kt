package com.miral.unsplash.ui.photodetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miral.unsplash.data.photo.model.Photo
import com.miral.unsplash.data.user.model.User
import com.miral.unsplash.domain.photo.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoDetailsViewModel @Inject constructor(
    private val repository: PhotoRepository
) : ViewModel() {

    private val _photoDetails = MutableStateFlow<PhotoDetails>(PhotoDetails.Empty)
    val photoDetails: StateFlow<PhotoDetails> = _photoDetails

    private val photoDetailsEventChannel = Channel<PhotoDetailsEvent>(Channel.CONFLATED)
    val photoDetailsEvent = photoDetailsEventChannel.receiveAsFlow()

    fun getPhotoDetails(id: String) = viewModelScope.launch(Dispatchers.IO) {
        val response = repository.getPhotoDetails(id)
        val result = response.body()
        if (response.isSuccessful && result != null) {
            _photoDetails.value = PhotoDetails.Success(result)
        } else {
            _photoDetails.value = PhotoDetails.Empty
        }
    }

    fun onPhotoClick(photo: Photo) = viewModelScope.launch {
        photoDetailsEventChannel.send(PhotoDetailsEvent.NavigateToPhotoZoom(photo))
    }

    fun onUserClick(user: User?) = viewModelScope.launch {
        photoDetailsEventChannel.send(PhotoDetailsEvent.NavigateToUserProfile(user))
    }

    fun onLocationClick(location: String?) = viewModelScope.launch {
        photoDetailsEventChannel.send(PhotoDetailsEvent.NavigateToMaps(location))
    }

    sealed class PhotoDetails {
        data class Success(val photoDetails: Photo) : PhotoDetails()
        object Empty : PhotoDetails()
    }

    sealed class PhotoDetailsEvent {
        data class NavigateToPhotoZoom(val photo: Photo) : PhotoDetailsEvent()
        data class NavigateToUserProfile(val user: User?) : PhotoDetailsEvent()
        data class NavigateToMaps(val location: String?) : PhotoDetailsEvent()
    }
}