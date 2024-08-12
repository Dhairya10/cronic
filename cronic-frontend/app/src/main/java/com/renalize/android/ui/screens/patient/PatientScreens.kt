package com.renalize.android.ui.screens.patient

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.renalize.android.R
import com.renalize.android.ui.screens.patient.hospitals.HospitalScreen
import com.renalize.android.ui.screens.patient.passbook.PassbookScreen
import com.renalize.android.ui.screens.patient.profile.ProfileScreen
import kotlinx.serialization.Serializable

@Composable
fun PatientScreens(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    var currentScreen by remember {
        mutableStateOf<PatientScreens>(PatientScreens.BottomNavScreens.Passbook)
    }

    Scaffold(
        bottomBar = {
            if(currentScreen is PatientScreens.BottomNavScreens){
                AppNavigationBar(
                    currentScreen = currentScreen,
                    onItemClicked = {
                        navController.navigate(it) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }

        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = PatientScreens.BottomNavScreens.Passbook,
            modifier = modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
        ) {

            composable<PatientScreens.BottomNavScreens.Passbook> {
                currentScreen = PatientScreens.BottomNavScreens.Passbook
                PassbookScreen()
            }

            composable<PatientScreens.BottomNavScreens.Profile> {
                currentScreen = PatientScreens.BottomNavScreens.Profile
                ProfileScreen(
                    onLogout = {
                        onLogout()
                    }
                )
            }

            composable<PatientScreens.BottomNavScreens.Hospitals> {
                currentScreen = PatientScreens.BottomNavScreens.Hospitals
                HospitalScreen()
            }

        }
    }
}

@Composable
fun AppNavigationBar(
    currentScreen: PatientScreens,
    onItemClicked: (PatientScreens.BottomNavScreens) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ){
        bottomNavItems.forEach {

            val selected = currentScreen == it.route
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        onItemClicked(it.route)
                    }
                    .background(
                        color = if(selected) Color(0xFFE9F5FF) else Color.Transparent
                    )
            ){
                Icon(
                    imageVector = ImageVector.vectorResource(id = it.icon),
                    contentDescription = it.title,
                    modifier = Modifier.size(32.dp),
                    tint = if(selected) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it.title,
                    color = if(selected) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = if(selected) FontWeight.W700 else FontWeight.Normal
                )
            }
        }
    }
}

data class BottomNavItems(
    @DrawableRes val icon: Int,
    val title: String,
    val route: PatientScreens.BottomNavScreens
)

val bottomNavItems = listOf(
    BottomNavItems(
        icon = R.drawable.ic_passbook,
        title = "Passbook",
        route = PatientScreens.BottomNavScreens.Passbook
    ),

    BottomNavItems(
        icon = R.drawable.ic_hospitals,
        title = "Hospitals",
        route = PatientScreens.BottomNavScreens.Hospitals
    ),
    BottomNavItems(
        icon = R.drawable.ic_profile,
        title = "Profile",
        route = PatientScreens.BottomNavScreens.Profile
    ),
)


sealed interface PatientScreens {

    sealed interface BottomNavScreens : PatientScreens{
        @Serializable
        data object Passbook : BottomNavScreens

        @Serializable
        data object Hospitals : BottomNavScreens

        @Serializable
        data object Profile : BottomNavScreens
    }
}

