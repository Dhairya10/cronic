package com.renalize.android.ui.screens.patient.bill_upload

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.renalize.android.R
import com.renalize.android.ui.components.DocUpload
import com.renalize.android.ui.components.UploadedScreen
import com.renalize.android.ui.components.UploadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillUploadScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val viewModel = hiltViewModel<BillUploadViewModel>()
    val state by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current

    Column{
        TopAppBar(
            title = { Text("Reimburse Money") },
            actions = {
                IconButton(
                    onClick = { onBack() }) {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.ic_close),
                        contentDescription = "close",
                    )
                }
            }
        )

        when(state){
            is BillUploadUiState.Uploading ->{
                UploadingScreen()
            }
            is BillUploadUiState.Success ->{
                UploadedScreen(
                    then = {
                        LaunchedEffect(Unit) {
                            viewModel.resetState()
                            onBack()
                        }
                    }
                )
            }
            else ->{
                if(state is BillUploadUiState.Error){
                    LaunchedEffect (Unit){
                        Toast.makeText(ctx, "Error", Toast.LENGTH_SHORT).show()
                    }
                }
                Column(
                    modifier = modifier
                        .padding(vertical = 8.dp, horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color(0xFFFFD979), RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF7E3), RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = "Info",
                                tint = Color(0xFF9B7000),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                "We are currently supporting only kidney patients. We will expand to other conditions soon",
                                fontSize = 12.sp,
                                color = Color(0xFF9B7000)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.padding(16.dp))
                    Text("Reimburse Money", fontSize = 18.sp, fontWeight = FontWeight.W600)
                    Text(
                        "Just upload documents and we’ll take care of the rest",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.padding(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F6FF), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Please upload these documents below", fontWeight = FontWeight.W500)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                " • Pharmacy Bills \n • Discharge Summary \n • Doctor Prescription",
                                color = Color.Gray,
                                lineHeight = 28.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(12.dp))
                    DocUpload(
                        onImageSelected = {
                            viewModel.uploadBill(it)
                        }
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetState()
        }
    }

}

