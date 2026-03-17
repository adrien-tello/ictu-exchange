package com.fanyiadrien.ictu_ex

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.fanyiadrien.ictu_ex.core.biometric.BiometricHelper
import com.fanyiadrien.ictu_ex.core.navigation.NavGraph
import com.fanyiadrien.ictu_ex.core.navigation.Screen
import com.fanyiadrien.ictu_ex.core.sensors.LightSensorManager
import com.fanyiadrien.ictu_ex.ui.theme.IctuExTheme
import com.fanyiadrien.ictu_ex.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var lightSensorManager: LightSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val sensorIsDark by lightSensorManager.isDark.collectAsState()
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.AUTO) }

            val isDark = when (themeMode) {
                ThemeMode.AUTO -> sensorIsDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            var startDestination by remember { mutableStateOf<String?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var biometricError by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                delay(1000) 
                
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    Log.d(TAG, "User is logged in: ${currentUser.email}. Triggering biometric...")
                    
                    if (BiometricHelper.isAvailable(this@MainActivity)) {
                        BiometricHelper.authenticate(
                            activity = this@MainActivity,
                            onSuccess = {
                                Log.d(TAG, "Biometric success!")
                                startDestination = Screen.Home.route
                                isLoading = false
                            },
                            onFailure = {
                                Log.w(TAG, "Biometric failed")
                            },
                            onError = { error ->
                                Log.e(TAG, "Biometric error: $error")
                                // If user cancels or error occurs, we don't proceed to home
                                biometricError = error
                            }
                        )
                    } else {
                        Log.w(TAG, "Biometric not available, skipping to Home")
                        startDestination = Screen.Home.route
                        isLoading = false
                    }
                } else {
                    Log.d(TAG, "No user logged in. Redirecting to Onboarding.")
                    startDestination = Screen.Onboarding.route
                    isLoading = false
                }
            }

            IctuExTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLoading) {
                        SplashScreen(errorMessage = biometricError) {
                            // Retry logic if biometric failed/errored
                            biometricError = null
                            recreate() 
                        }
                    } else {
                        val navController = rememberNavController()
                        NavGraph(
                            navController = navController,
                            startDestination = startDestination ?: Screen.Onboarding.route,
                            auth = auth,
                            themeMode = themeMode,
                            onThemeModeChange = { themeMode = it }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SplashScreen(errorMessage: String? = null, onRetry: () -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = "ICTU-Ex Logo",
                    modifier = Modifier.size(100.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage != null) {
                    Text(
                        text = "Authentication Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text("Retry Verification")
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Verifying Identity...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lightSensorManager.register()
    }

    override fun onPause() {
        super.onPause()
        lightSensorManager.unregister()
    }
}