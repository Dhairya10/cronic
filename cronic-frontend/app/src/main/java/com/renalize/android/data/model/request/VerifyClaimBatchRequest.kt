package com.renalize.android.data.model.request

data class VerifyClaimBatchRequest(
    val documents: List<VerifyClaimRequest>
)