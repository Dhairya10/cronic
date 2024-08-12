package com.renalize.android.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renalize.android.R
import com.renalize.android.ui.components.AppButton
import com.renalize.android.ui.theme.HealthcareTheme

@Composable
fun PhoneInputScreen(
    modifier: Modifier = Modifier,
    onProceed: (String) -> Unit = {}
) {

    var phone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box{
            Image(
                painter = painterResource(R.drawable.background),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.size(30.dp))
                Row{
                    Icon(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.logo_text),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "Get financial support for your \n  medical expenses.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(85.dp))
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Divider(modifier = Modifier.width(40.dp), color = Color.LightGray)
            Text(
                text = "Login or sign up",
                color = Color.Gray,
            )
            Divider(modifier = Modifier.width(40.dp), color = Color.LightGray)
        }

        Spacer(modifier = Modifier.size(22.dp))
        Text("What’s your phone number?", fontWeight = FontWeight.W600, fontSize = 16.sp)
        Spacer(modifier = Modifier.size(12.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = {
                if (it.length <= 10) {
                    phone = it
                }
            },
            prefix = {
                Text("+91")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            label = {
                Text("Phone number", color = Color.LightGray)
            },
            shape = MaterialTheme.shapes.small
        )
        Spacer(modifier = Modifier.size(16 .dp))
        AppButton(
            onClick = {
                onProceed(phone)
            },
            modifier = Modifier
                .padding(horizontal = 30.dp),
            enabled = phone.length == 10
        ) {
            Text("Get OTP", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.weight(2f))
    }
}

@Preview(showBackground = true)
@Composable
fun PhoneInputScreenPreview() {
    HealthcareTheme {
        PhoneInputScreen()
    }
}