package com.renalize.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppFilterChip(
    text: String,
    isSelected: Boolean,
    selectedBackground: Color,
    selectedContentColor: Color,
    selectedStroke: Color,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if(isSelected) selectedBackground else Color.Transparent
    val contentColor = if(isSelected) selectedContentColor else Color(0xFF595959)
    val strokeColor = if(isSelected) selectedStroke else Color.LightGray

    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(1.dp, strokeColor, RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onSelected() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ){
        Text(
            text = text,
            color = contentColor,
            fontSize = 12.sp
        )
    }
}