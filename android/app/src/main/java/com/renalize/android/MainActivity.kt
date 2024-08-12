package com.renalize.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.renalize.android.ui.screens.auth.AuthScreen
import com.renalize.android.ui.screens.onboarding.OnBoarding
import com.renalize.android.ui.screens.patient.PatientScreens
import com.renalize.android.ui.theme.HealthcareTheme
import com.renalize.android.util.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        firebaseAuth.currentUser?.getIdToken(false)?.addOnSuccessListener {
            preferenceManager.putString(PreferenceManager.Keys.USER_TOKEN, it.token!!)
        }
        setContent {

            HealthcareTheme{

                val navController = rememberNavController()
                val onBoardingCompleted =
                    preferenceManager.getBoolean(PreferenceManager.Keys.IS_LOGGED_IN)

                NavHost(
                    navController = navController,
                    startDestination = if (onBoardingCompleted) Screen.PatientHome else Screen.Auth,
                ) {
                    composable<Screen.Auth> {
                        AuthScreen(
                             onNewUserLogin = {
                                navController.navigate(Screen.OnBoarding)
                             },
                            onExistingUserLogin = {
                                preferenceManager.putBoolean(
                                    PreferenceManager.Keys.IS_LOGGED_IN,
                                    true
                                )
                                navController.navigate(Screen.PatientHome){
                                    popUpTo(Screen.PatientHome)
                                }
                            }
                        )
                    }
                    composable<Screen.OnBoarding> {
                        OnBoarding(
                            onBack = {
                                navController.popBackStack()
                            },
                            onFinished = {
                                preferenceManager.putBoolean(
                                    PreferenceManager.Keys.IS_LOGGED_IN,
                                    true
                                )
                                navController.navigate(Screen.PatientHome) {
                                    popUpTo(Screen.PatientHome)
                                }
                            }
                        )
                    }

                    composable<Screen.PatientHome> {
                        PatientScreens(
                            onLogout = {
                                navController.navigate(Screen.Auth) {
                                    popUpTo(Screen.PatientHome) { inclusive = true }
                                }
                                preferenceManager.clear()
                            }
                        )
                    }
                }
            }
        }
    }
}

sealed interface Screen {
    @Serializable
    data object Auth : Screen

    @Serializable
    data object OnBoarding : Screen

    @Serializable
    data object PatientHome : Screen
}