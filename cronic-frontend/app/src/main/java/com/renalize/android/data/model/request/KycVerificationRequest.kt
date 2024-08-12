package com.renalize.android.data.model.request


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class KycVerificationRequest(

    @SerializedName("document_type")
    val documentType: String,

    @SerializedName("file_uri")
    val fileUri: String
)