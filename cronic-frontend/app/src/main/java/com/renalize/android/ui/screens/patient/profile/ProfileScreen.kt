package com.renalize.android.ui.screens.patient.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.renalize.android.R
import com.renalize.android.data.model.common.AadharData
import com.renalize.android.data.model.common.Address
import com.renalize.android.data.model.common.BankAccountData
import com.renalize.android.data.model.common.KycData
import com.renalize.android.data.model.common.PanData
import com.renalize.android.data.model.response.PatientData
import com.renalize.android.ui.components.AppButton
import com.renalize.android.ui.components.AppModalBottomSheet
import com.renalize.android.ui.components.ErrorScreen
import com.renalize.android.ui.components.LoadingScreen

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {

    val viewmodel = hiltViewModel<ProfileViewModel>()
    val uiState by viewmodel.uiState.collectAsState()

    LaunchedEffect(Unit){
       viewmodel.getProfileData()
    }

    Column{
        Spacer(modifier = Modifier.height(12.dp))
        Text("Profile", fontWeight = FontWeight.W600, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(37.dp))
        when(val currState = uiState){
            is ProfileUiState.Loading -> {
                LoadingScreen()
            }

            is ProfileUiState.Success ->{
                ProfileScreenContent(
                    modifier = modifier,
                    onLogout = onLogout,
                    data = currState.profileData.patientData ?: samplePatientDataResponse,
                )
            }

            is ProfileUiState.Error ->{
                ErrorScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    data: PatientData
) {

    var showFeedbackBottomSheet by remember { mutableStateOf(false) }

    if(showFeedbackBottomSheet){
        var feedbackText by remember { mutableStateOf("") }

        AppModalBottomSheet(onDismissRequest = { showFeedbackBottomSheet = false}){

            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ){
                Text("Thanks for your feedback!", fontWeight = FontWeight.W500, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("We value your feedback and will use it to improve our services.", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange ={ feedbackText = it },
                    label = { Text("Feedback") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                AppButton(onClick = { }) {
                    Text("Submit")
                }
            }

        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .then(modifier)
    ){

        ProfileItem(
            icon = ImageVector.vectorResource(id = R.drawable.ic_profile),
            title = "Personal Details"
        ) {
            Column{
                Row{
                    Column(
                        modifier = Modifier.weight(1f)
                    ){
                        Text("Name", fontSize = 12.sp)
                        Text(data.kycData.aadharData.name, fontSize = 14.sp, fontWeight = FontWeight.W500)
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ){
                        Text("Mobile Number", fontSize = 12.sp)
                        Text(data.contactNum, fontSize = 14.sp, fontWeight = FontWeight.W500)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row{
                    Column(
                        modifier = Modifier.weight(1f)
                    ){
                        Text("D.O.B", fontSize = 12.sp)
                        Text(data.kycData.aadharData.dob, fontSize = 14.sp, fontWeight = FontWeight.W500)
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ){
                        Text("Gender", fontSize = 12.sp)
                        Text(data.kycData.aadharData.gender, fontSize = 14.sp, fontWeight = FontWeight.W500)
                    }
                }
            }
        }

        ProfileItem(
            icon = ImageVector.vectorResource(id = R.drawable.ic_bank),
            title = "Bank Details"
        ) {
            Column{
                Row{
                    Column(
                        modifier = Modifier.weight(1f)
                    ){
                        Text("Bank Name", fontSize = 12.sp)
                        Text(data.kycData.bankAccountData.bankName, fontSize = 14.sp, fontWeight = FontWeight.W500)
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ){
                        Text("Account Number", fontSize = 12.sp)
                        Text(data.kycData.bankAccountData.accountNumber, fontSize = 14.sp, fontWeight = FontWeight.W500)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row{
                    Column(
                        modifier = Modifier.weight(1f)
                    ){
                        Text("IFSC", fontSize = 12.sp)
                        Text(data.kycData.bankAccountData.ifscCode, fontSize = 14.sp, fontWeight = FontWeight.W500)
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ){
                        Text("Branch", fontSize = 12.sp)
                        Text(data.kycData.bankAccountData.branchName, fontSize = 14.sp, fontWeight = FontWeight.W500)
                    }
                }
            }
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable {
                    showFeedbackBottomSheet = true
                }
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_feedback), contentDescription = "Icon", tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Feedback", fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Arrow Right",
                tint = Color.Gray
            )
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )

        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable {
                    onLogout()
                }
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_logout),
                contentDescription = "logout",
                tint = Color(0xFFE57373)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontSize = 16.sp, modifier = Modifier.weight(1f), color = Color(0xFFE57373))
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Arrow Right",
                tint = Color(0xFFE57373)
            )
        }

    }
}

@Composable
fun ColumnScope.ProfileItem(
    icon : ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit)
) {
    var isOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                isOpen = !isOpen
            }
            .padding(vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = "Icon", tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (isOpen) (Icons.Filled.KeyboardArrowDown) else (Icons.Filled.KeyboardArrowRight),
            contentDescription = "Arrow Right",
            tint = Color.Gray
        )
    }

    AnimatedVisibility(isOpen) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) { content() }
    }

    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    )
}



val samplePatientDataResponse = PatientData(
    patientId = "patient123",
    uhid = "UHID123456",
    contactNum = "1234567890",
    healthCondition = "chronic_kidney_disease",
    primaryDoctorName = "Dr. John Doe",
    primaryHealthcareProvider = "Healthcare Provider Name",
    kycData = KycData(
        aadharData = AadharData(
            aadharNumber = "123456789012",
            name = "John Doe",
            dob = "2024-08-08",
            gender = "male",
            address = Address(
                street = "Street Name",
                city = "City Name",
                state = "State Name",
                pincode = "123456"
            ),
        ),
        panData = PanData(
            name = "John Doe",
            panNumber = "ABCDE1234F"
        ),
        bankAccountData = BankAccountData(
            accountHolderName = "John Doe",
            accountNumber = "1234567890",
            bankName = "Bank Name",
            branchName = "Branch Name",
            ifscCode = "IFSC1234"
        ),
        incomeLevel = "less_than_2"
    )
)


