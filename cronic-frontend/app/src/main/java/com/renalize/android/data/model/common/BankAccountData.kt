package com.renalize.android.data.model.common


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BankAccountData(
    @SerializedName("account_holder_name")
    val accountHolderName: String,

    @SerializedName("account_number")
    val accountNumber: String,

    @SerializedName("bank_name")
    val bankName: String,

    @SerializedName("branch_name")
    val branchName: String,

    @SerializedName("ifsc_code")
    val ifscCode: String
)