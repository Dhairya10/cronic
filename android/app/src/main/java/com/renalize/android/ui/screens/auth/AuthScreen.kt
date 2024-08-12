package com.renalize.android.ui.screens.auth

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.renalize.android.MainActivity
import kotlinx.serialization.Serializable

@Composable
fun MainActivity.AuthScreen(
    modifier: Modifier = Modifier,
    onNewUserLogin: () -> Unit,
    onExistingUserLogin: () -> Unit
) {

    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = AuthScreens.PhoneInputScreen,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            composable<AuthScreens.PhoneInputScreen> {
                PhoneInputScreen(
                     onProceed = { phone ->
                        navController.navigate(AuthScreens.OTPScreen(phone))
                    }
                )
            }

            composable<AuthScreens.OTPScreen> {
                //val args = it.toRoute<AuthScreens.OTPScreen>()
                OTPScreen(
                    onBack = {
                      navController.popBackStack()
                    },
                    onNewUser = {
                        onNewUserLogin()
                    },
                    onExistingUser = {
                        onExistingUserLogin()
                    }
                )
            }

        }
    }
}

sealed class AuthScreens{
    @Serializable
    data object PhoneInputScreen: AuthScreens()
    @Serializable
    data class OTPScreen(val phone:String): AuthScreens()
}

