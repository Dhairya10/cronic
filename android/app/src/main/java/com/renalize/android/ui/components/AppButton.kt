package com.renalize.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppButton(
    onClick : () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content : @Composable () -> Unit
) {
    Button(
        onClick = { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .then(modifier),
        shape = MaterialTheme.shapes.small,
        enabled = enabled
    ) {
        content()
    }
}

@Composable
fun AppOutlinedButton(
    onClick : () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content : @Composable () -> Unit
) {
    OutlinedButton(
        onClick = { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .then(modifier),
        shape = MaterialTheme.shapes.small,
        enabled = enabled,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        content()
    }
}