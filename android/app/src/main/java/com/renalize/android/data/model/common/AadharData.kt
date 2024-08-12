package com.renalize.android.data.model.common


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AadharData(

    @SerializedName("aadhar_number")
    val aadharNumber: String,

    val address: Address,

    @SerializedName("date_of_birth")
    val dob: String,

    val gender: String,

    val name: String
)