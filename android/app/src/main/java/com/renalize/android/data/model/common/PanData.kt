package com.renalize.android.data.model.common


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PanData(

    val name: String,

    @SerializedName("pan_number")
    val panNumber: String
)