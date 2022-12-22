package com.miral.unsplash.data.topic.model

import android.os.Parcelable
import com.miral.unsplash.data.user.model.User
import kotlinx.parcelize.Parcelize

@Parcelize
data class Topic(
    val id: String,
    val slug: String,
    val title: String,
    val description: String?,
    val owners: List<User>?,
) : Parcelable
