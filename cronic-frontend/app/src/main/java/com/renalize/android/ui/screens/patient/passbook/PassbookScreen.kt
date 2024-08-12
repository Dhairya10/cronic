package com.renalize.android.ui.screens.patient.passbook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.renalize.android.R
import com.renalize.android.data.model.response.Bill
import com.renalize.android.ui.components.AppButton
import com.renalize.android.ui.components.AppCard
import com.renalize.android.ui.components.AppFilterChip
import com.renalize.android.ui.components.AppModalBottomSheet
import com.renalize.android.ui.components.LoadingScreen
import com.renalize.android.ui.screens.patient.bill_upload.BillUploadScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassbookScreen(
    modifier: Modifier = Modifier
) {

    val viewmodel: PassbookViewModel = hiltViewModel()
    val uiState by viewmodel.uiState.collectAsState()
    val bills by viewmodel.bills

    LaunchedEffect(Unit) {
        viewmodel.getBills()
    }


    var showBillClaimBottomSheet by remember {
        mutableStateOf(false)
    }

    AnimatedVisibility(showBillClaimBottomSheet) {
        AppModalBottomSheet(
            onDismissRequest = {
                showBillClaimBottomSheet = false
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            BillUploadScreen(onBack = { showBillClaimBottomSheet = false })
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        //TOP BAR
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp, top = 12.dp)
        ) {
            Text("Welcome, ${viewmodel.name}", fontWeight = FontWeight.W600)
            Spacer(modifier = Modifier.weight(1f))
            if(bills.isNotEmpty()){
                Text(
                    "+ Upload New Bill",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W600,
                    modifier = Modifier.clickable {
                        showBillClaimBottomSheet = true
                    }
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
        }

        when (uiState) {
            is PassbookViewModel.UIState.Loading -> {
                LoadingScreen(modifier = modifier)
            }

            is PassbookViewModel.UIState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .then(modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Something went wrong",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W600
                    )
                }
            }

            is PassbookViewModel.UIState.Success -> {
                if (bills.isEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    EmptyPassbookScreenContent(
                        onUploadClick = { showBillClaimBottomSheet = true },
                        modifier = modifier
                    )
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    PassbookScreenContent(
                        bills,
                        modifier = modifier,
                        onNewClaimClick = { showBillClaimBottomSheet = true }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassbookScreenContent(
    bills: List<Bill>,
    onNewClaimClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember {
        mutableStateOf(PassbookFilter.ALL)
    }

    val pendingBills = bills.filter { it.status == "pending" }
    val verifiedBills = bills.filter { it.status == "verified" }
    val rejectedBills = bills.filter { it.status == "rejected" }
    var filteredBills by remember {
        mutableStateOf(bills)
    }

    var showFailureBottomSheet by remember {
        mutableStateOf(false)
    }
    var failureMessage by remember {
        mutableStateOf("")
    }


    LaunchedEffect(selectedFilter) {
        filteredBills = when (selectedFilter) {
            PassbookFilter.ALL -> {
                bills
            }

            PassbookFilter.PENDING -> {
                pendingBills
            }

            PassbookFilter.REJECTED -> {
                rejectedBills
            }

            PassbookFilter.VERIFIED -> {
                verifiedBills
            }
        }
    }

    if (showFailureBottomSheet) {
        AppModalBottomSheet(
            onDismissRequest = { showFailureBottomSheet = false },
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Claim Verification Failed",
                        fontWeight = FontWeight.W500,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showFailureBottomSheet = false }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
                            contentDescription = "Close",
                        )
                    }
                }
                Spacer(modifier = Modifier.size(24.dp))
                AppCard(contentColor = Color(0xFFD63646),
                    backgroundColor = Color(0xFFFFE9E5),
                    strokeColor = Color(0xFFFF5F46),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "info",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(failureMessage, fontWeight = FontWeight.W500, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.size(30.dp))
                AppButton(onClick = {
                    showFailureBottomSheet = false
                    onNewClaimClick()
                }) {
                    Text("Raise new claim", fontWeight = FontWeight.W700, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.size(30.dp))
            }

        }
    }

    LazyColumn {
        item {
            Column {
                AppCard(
                    contentColor = Color(0xFF22B153),
                    backgroundColor = Color(0xFFF3FFF7),
                    strokeColor = Color(0xFF82FFAE),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_money),
                            contentDescription = "Home",
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "₹ ${verifiedBills.sumOf { it.amount }}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W700
                            )
                            Text("Verification Completed", fontWeight = FontWeight.W500)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                AppCard(
                    contentColor = Color(0xFF9B7000),
                    backgroundColor = Color(0xFFFFF9EA),
                    strokeColor = Color(0xFFFFD979),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_doc),
                            contentDescription = "Home",
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "₹${pendingBills.sumOf { it.amount }} (Verification pending)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W700
                            )
                            Text("Total Bills: ${pendingBills.size}", fontWeight = FontWeight.W500)
                        }
                    }
                }
                Spacer(modifier = Modifier.size(24.dp))
                Text("Overview", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.size(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    PassbookFilter.entries.forEach {
                        AppFilterChip(
                            text = it.value,
                            isSelected = selectedFilter == it,
                            selectedBackground = it.selectedBackground,
                            selectedContentColor = it.selectedContentColor,
                            selectedStroke = it.selectedStroke,
                            onSelected = { selectedFilter = it }
                        )
                    }
                }
            }
        }
        items(filteredBills) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .clickable {
                        if (it.status == "rejected") {
                            showFailureBottomSheet = true
                            failureMessage = it.reasoning
                        }
                    }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_doc),
                    contentDescription = null,
                    tint = Color.Gray,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(it.billId, fontWeight = FontWeight.W500)
                    Text(it.date.substring(0, 9), color = Color.Gray, fontSize = 12.sp)
                }
                Column {
                    Text("₹ ${it.amount}", fontWeight = FontWeight.W500, fontSize = 16.sp)
                    if (it.status == "rejected") {
                        Text(
                            it.status,
                            color = PassbookFilter.valueOf(it.status.toUpperCase()).selectedContentColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPassbookScreenContent(
    modifier: Modifier = Modifier,
    onUploadClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .then(modifier)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hospital),
                contentDescription = null,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
            )

            Text(
                "You are eligible for 40% reimbursement on your medical expenditure. Submit your bills below to get the money",
                lineHeight = 19.sp,
            )

            AppButton(onClick = { onUploadClick() }) {
                Text("Reimburse Money", fontWeight = FontWeight.W700, fontSize = 14.sp)
            }
        }
    }


}

enum class PassbookFilter(
    val value: String,
    val selectedBackground: Color,
    val selectedContentColor: Color,
    val selectedStroke: Color
) {
    ALL(
        "All Bills",
        Color(0xFFECECEC),
        Color(0xFF595959),
        Color(0xFF595959)
    ),
    PENDING(
        "Pending", Color(0xFFFFF9EA),
        Color(0xFF9B7000),
        Color(0xFFFFD979)
    ),
    REJECTED(
        "Rejected",
        Color(0xFFFFE9E5),
        Color(0xFFD63646),
        Color(0xFFFF5F46)
    ),
    VERIFIED(
        "Verified",
        Color(0xFFF3FFF7),
        Color(0xFF22B153),
        Color(0xFF82FFAE)
    )
}
