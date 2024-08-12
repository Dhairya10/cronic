package com.renalize.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renalize.android.ui.theme.HealthcareTheme

@Composable
fun SteppedProgressBar(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier.fillMaxWidth().then(modifier),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ){
        repeat(totalSteps){
            StepIndicator(
                isCompleted = it < currentStep,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StepIndicator(
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {

    val color = if (isCompleted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    }

    Box(
        modifier = modifier
            .height(5.dp)
            .background(
                color = color,
                shape = CircleShape
            )
    )
}

@Preview(showBackground = true)
@Composable
private fun SteppedProgressBarPreview() {
    HealthcareTheme {
        SteppedProgressBar(
            totalSteps = 3,
            currentStep = 1
        )
    }
}