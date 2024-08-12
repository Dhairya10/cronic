package com.renalize.android.data.model.common


import androidx.annotation.Keep

@Keep
data class Address(
    val city: String,
    val pincode: String,
    val state: String,
    val street: String
)