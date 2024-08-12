package com.renalize.android.data.model.response


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BillHistoryResponse(
    val bills: List<Bill>
)

@Keep
data class Bill(
    val amount: Int,

    @SerializedName("id")
    val billId: String,

    val date: String,

    @SerializedName("patient_id")
    val patientId: String,

    val reasoning: String,

    val status: String,

    val type: String
)