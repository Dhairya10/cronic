package com.renalize.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.renalize.android.R

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    message: String = ""
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(message, fontSize = 16.sp)
    }
}

@Composable
fun UploadingScreen(modifier: Modifier = Modifier) {
    Popup {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(color = Color.Black.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.uploading))
            val progress by animateLottieCompositionAsState(
                composition,
                iterations = LottieConstants.IterateForever
            )

            Column {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    "Uploading...",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.W600
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UploadedScreen(
    then: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.uploaded))
    val progress by animateLottieCompositionAsState(composition, iterations = 1)

    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .then(modifier),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Column {
            val width = rememberTextMeasurer().measure(
                "Documents Submitted",
                TextStyle.Default.copy(fontSize = 18.sp, fontWeight = FontWeight.W600)
            ).size.width.pxToDp()
            Text("Documents Submitted", fontSize = 18.sp, fontWeight = FontWeight.W600)
            Text(
                "Now, we will verify the documents and get back to you in 4-5 days",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.width(width),
                textAlign = TextAlign.Center
            )
        }

    }

    if (progress == 1f) then()
}

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    message: String = "Something went wrong"
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, fontSize = 16.sp)
    }

}

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }