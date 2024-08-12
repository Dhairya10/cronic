package com.renalize.android.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.renalize.android.MainActivity
import com.renalize.android.R
import com.renalize.android.ui.components.AppButton
import com.renalize.android.ui.components.LoadingScreen
import com.renalize.android.ui.theme.HealthcareTheme

@Composable
fun MainActivity.OTPScreen(
    onBack: () -> Unit,
    onNewUser : () -> Unit,
    onExistingUser: () -> Unit
) {

    val viewModel = hiltViewModel<AuthViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit){
        viewModel.sendOtp(this@OTPScreen)
    }

    OTPScreenContent(
        uiState = uiState,
        phoneNumber = uiState.phoneNumber,
        onOTPEnter = { viewModel.updateEnteredOTP(it) },
        onOTPSubmit = { viewModel.signInWithOtp() },
        onBack = { onBack() },
        onNewUser = { onNewUser() },
        onExistingUser = { onExistingUser() }
    )
}

@Composable
fun OTPScreenContent(
    uiState : PhoneAuthState,
    phoneNumber : String,
    onOTPEnter: (String) -> Unit,
    onOTPSubmit : () -> Unit,
    onBack: () -> Unit,
    onNewUser : () -> Unit,
    onExistingUser: () -> Unit,
) {

    if(uiState.success){
        LaunchedEffect(Unit){
            if(uiState.newUser) onNewUser()
            else onExistingUser()
        }
    }

    if(uiState.isLoading){
        if(uiState.isOtpSent) LoadingScreen(message = "validating OTP")
        else LoadingScreen(message = "sending OTP")
        return
    }

    Image(
        painter = painterResource(id = R.drawable.background),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.Crop
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        IconButton(onClick = { onBack() }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back")
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("Please enter the OTP sent to $phoneNumber", fontSize = 20.sp, fontWeight = FontWeight.W600)
        Spacer(modifier = Modifier.height(24.dp))
        BasicTextField(
            value = uiState.otp,
            onValueChange = { onOTPEnter(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            keyboardActions = KeyboardActions(
                onDone = { onOTPSubmit() }
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(6) {
                    val char = if (it >= uiState.otp.length) " " else uiState.otp[it].toString()
                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .sizeIn(maxWidth = 48.dp, maxHeight = 48.dp),
                        border = if (it == uiState.otp.length || (uiState.otp.length == 6 && it == 5)) BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.primary
                        ) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(char, fontSize = 20.sp)
                        }
                    }
                }
            }
        }
        if (uiState.error != null) {
            Text(uiState.error, color = Color.Red)
        }
        Spacer(modifier = Modifier.height(16.dp))
        AppButton(
            onClick = { onOTPSubmit() },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = uiState.otp.length == 6
        ) {
            Text("Submit", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.weight(2f))

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOTPScreen(){
    HealthcareTheme {
        OTPScreenContent(
            uiState = PhoneAuthState(isOtpSent = true),
            phoneNumber = "1234567890",
            onOTPEnter = {},
            onOTPSubmit = {},
            onBack = {},
            onNewUser = {},
            onExistingUser = {}
        )
    }
}