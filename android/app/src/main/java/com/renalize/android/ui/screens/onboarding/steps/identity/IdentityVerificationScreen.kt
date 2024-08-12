package com.renalize.android.ui.screens.onboarding.steps.identity

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.renalize.android.ui.components.PhotoPicker
import com.renalize.android.ui.components.UploadingScreen

@Composable
fun IdentityVerificationScreen(
    onProceed: () -> Unit,
    modifier: Modifier = Modifier
) {

    val viewModel = hiltViewModel<ViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val ctx = LocalContext.current



    if (uploadState is UploadState.Uploaded) {
        LaunchedEffect(Unit) {
            Toast.makeText(ctx, "Uploaded successfully", Toast.LENGTH_SHORT).show()
        }
    }

    if (uploadState is UploadState.Error) {
        LaunchedEffect(Unit) {
            Toast.makeText(ctx, (uploadState as UploadState.Error).message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    if (uploadState is UploadState.Uploading) {
        UploadingScreen()
    }

    var activeItem by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = modifier.padding(20.dp))

        Text("Identity verification", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Next, let’s verify your identity.")

        Spacer(modifier = modifier.padding(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            UploadItem(
                itemPosition = 1,
                title = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.W600, fontSize = 18.sp)) {
                        append("Aadhar card")
                    }
                },
                isExpanded = uiState.aadharCardFrontUri == null || uiState.aadharCardBackUri == null,
                isCompleted = uiState.aadharCardFrontUri != null && uiState.aadharCardBackUri != null
            ) {
                PhotoPicker(
                    title = "Front of card",
                    selectedImageUri = uiState.aadharCardFrontUri,
                    onImageSelected = { viewModel.onAadharCardFrontSelected(it) }
                )
                PhotoPicker(
                    title = "Back of card",
                    selectedImageUri = uiState.aadharCardBackUri,
                    onImageSelected = {
                        viewModel.onAadharCardBackSelected(it)
                    },
                    enabled = uiState.aadharCardFrontUri != null
                )
            }
            UploadItem(
                itemPosition = 2,
                title = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.W600, fontSize = 18.sp)) {
                        append("PAN Card")
                    }
                },
                isExpanded = uiState.aadharCardFrontUri != null && uiState.aadharCardBackUri != null && uiState.panCardUri == null,
                isCompleted = uiState.panCardUri != null
            ) {
                PhotoPicker(
                    title = "front of card",
                    selectedImageUri = uiState.panCardUri,
                    onImageSelected = {
                        viewModel.onPanCardSelected(it)
                        activeItem = 3
                    }
                )
            }
            UploadItem(
                itemPosition = 3,
                title = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.W600, fontSize = 18.sp)) {
                        append("Bank passbook")
                    }
                },
                isExpanded = uiState.aadharCardFrontUri != null &&
                        uiState.aadharCardBackUri != null &&
                        uiState.panCardUri != null &&
                        uiState.bankPassbookUri == null,
                isCompleted = uiState.bankPassbookUri != null
            ) {
                PhotoPicker(
                    title = "First page of passbook",
                    selectedImageUri = uiState.bankPassbookUri,
                    onImageSelected = {
                        viewModel.onBankPassbookSelected(it)
                        activeItem = 4
                    }
                )
            }

            UploadItem(
                itemPosition = 4,
                title = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.W600, fontSize = 18.sp)) {
                        append("UHID")
                    }
                    withStyle(SpanStyle(fontSize = 14.sp, color = Color.Gray)) {
                        append(" (optional)")
                    }
                },
                isExpanded = uiState.aadharCardFrontUri != null &&
                        uiState.aadharCardBackUri != null &&
                        uiState.panCardUri != null &&
                        uiState.bankPassbookUri != null,
                isCompleted = uiState.uhid.isNotBlank()
            ) {
                OutlinedTextField(
                    value = uiState.uhid,
                    onValueChange = { viewModel.onUHIDChanged(it) },
                    label = { Text("Enter your UHID") },
                    modifier = modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = modifier.padding(16.dp))
        Spacer(modifier = modifier.weight(1f))

        Button(
            onClick = { onProceed() },
            enabled = activeItem == 4,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun UploadItem(
    itemPosition: Int,
    title: AnnotatedString,
    isExpanded: Boolean,
    isCompleted : Boolean,
    modifier: Modifier = Modifier,
    child: @Composable ColumnScope.() -> Unit,
) {
    Column{
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
        ){
            if(isCompleted){
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Check Circle",
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF4CAF50)
                )
            }else{
                Box(
                    modifier = modifier
                        .background(
                            color = if (isExpanded) MaterialTheme.colorScheme.primary else Color.LightGray,
                            CircleShape
                        )
                        .size(20.dp)
                ) {
                    Text(
                        itemPosition.toString(),
                        fontSize = 12.sp,
                        color = if(isExpanded) Color.White else Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = modifier.padding(6.dp))
            Text(title)
        }
        AnimatedVisibility(isExpanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier.padding(start = 28.dp, top = 6.dp)
            ){ child() }
        }
    }
}
