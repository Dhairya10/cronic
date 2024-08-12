package com.renalize.android.ui.screens.patient.hospitals

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.renalize.android.data.api.HospitalsItem
import com.renalize.android.ui.components.ErrorScreen
import com.renalize.android.ui.components.LoadingScreen

@Composable
fun HospitalScreen(modifier: Modifier = Modifier) {

    val viewModel = hiltViewModel<HospitalsViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    Column{
        Spacer(modifier = Modifier.height(12.dp))
        Text("Hospital Network", fontWeight = FontWeight.W600, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(37.dp))
        when(val currState = uiState){
            is HospitalUiState.Loading -> {
                LoadingScreen(modifier = modifier)
            }
            is HospitalUiState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .padding(20.dp)
                        .then(modifier)
                ){
                    items(currState.data){
                        HospitalItemView(it)
                    }
                }
            }
            is HospitalUiState.Error -> {
                ErrorScreen(modifier = modifier)
            }
        }
    }

}

@Composable
fun HospitalItemView(
    hospitalsItem: HospitalsItem,
    modifier: Modifier = Modifier
) {

    val ctx = LocalContext.current

    val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${hospitalsItem.contactNumber}"))
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(hospitalsItem.websiteLink))
    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(hospitalsItem.googleMapsLink))

    OutlinedCard(
        border = BorderStroke(1.dp, Color.LightGray),
    ){
        Column(
            modifier = modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        ){
            Text(hospitalsItem.name, fontWeight = FontWeight.W700)
            Spacer(modifier = Modifier.height(4.dp))
            Row{
                Text("General Hospital • ", fontSize = 12.sp, color = Color.Gray)
                Text("open 24 hours", fontSize = 12.sp, color = Color(0xFF22B153))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(hospitalsItem.address, fontSize = 12.sp, color = Color.Black.copy(0.6f), lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ){
                OutlinedIconTextButton(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_call),
                    text = "Call",
                    onClick = { ctx.startActivity(callIntent) }
                )

                OutlinedIconTextButton(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_web),
                    text = "Website",
                    onClick = { ctx.startActivity(webIntent) }
                )

                OutlinedIconTextButton(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_direction),
                    text = "Directions",
                    onClick = { ctx.startActivity(mapIntent) }
                )
            }
        }
    }
}

@Composable
fun OutlinedIconTextButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = {onClick()},
        modifier = modifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ){
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text)
        }
    }
}