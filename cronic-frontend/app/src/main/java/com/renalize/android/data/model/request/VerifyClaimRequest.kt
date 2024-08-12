package com.renalize.android.data.model.request

import com.google.gson.annotations.SerializedName

data class VerifyClaimRequest(
    @SerializedName("file_uri")
    val fileUri : String,
    @SerializedName("document_type")
    val documentType: String
)
