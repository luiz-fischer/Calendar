package com.example.calendar

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.calendar.ui.CalendarScreen
import com.example.calendar.ui.CalendarViewModel
import com.example.calendar.ui.theme.CalendarTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: CalendarViewModel by viewModels()

    private val gpsLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            fetchLocation()
        } else {
            Toast.makeText(this, "O GPS precisa estar ligado para detectar SC", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarTheme {
                val locationPermissionState = rememberPermissionState(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalendarScreen(
                        viewModel = viewModel,
                        onRequestLocation = {
                            if (locationPermissionState.status.isGranted) {
                                checkSettingsAndFetchLocation()
                            } else {
                                locationPermissionState.launchPermissionRequest()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun checkSettingsAndFetchLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            fetchLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    gpsLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e("MainActivity", "Erro GPS", sendEx)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Toast.makeText(this, "Buscando localização...", Toast.LENGTH_SHORT).show()
        
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    processLocation(location.latitude, location.longitude)
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) processLocation(lastLoc.latitude, lastLoc.longitude)
                        else Toast.makeText(this, "Sinal de GPS fraco. Tente novamente em local aberto.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .addOnFailureListener {
                Log.e("MainActivity", "Erro GPS", it)
            }
    }

    private fun processLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale("pt", "BR"))
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        updateLocationData(address.adminArea, address.locality ?: address.subAdminArea)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    updateLocationData(address.adminArea, address.locality ?: address.subAdminArea)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro Geocoder", e)
        }
    }

    private fun updateLocationData(adminArea: String?, locality: String?) {
        val stateCode = stateToUF(adminArea)
        val city = locality ?: ""
        
        runOnUiThread {
            viewModel.onLocationDetected(stateCode, city)
            Toast.makeText(this, "Localizado: $city - $stateCode", Toast.LENGTH_LONG).show()
        }
    }

    private fun stateToUF(stateName: String?): String {
        val cleanName = stateName?.lowercase()?.trim() ?: ""
        
        return when {
            cleanName.contains("catarina") || cleanName == "sc" -> "SC"
            cleanName.contains("são paulo") || cleanName.contains("sao paulo") || cleanName == "sp" -> "SP"
            cleanName.contains("rio de janeiro") || cleanName == "rj" -> "RJ"
            cleanName.contains("paraná") || cleanName.contains("parana") || cleanName == "pr" -> "PR"
            cleanName.contains("rio grande do sul") || cleanName == "rs" -> "RS"
            cleanName.contains("minas gerais") || cleanName == "mg" -> "MG"
            cleanName.contains("bahia") || cleanName == "ba" -> "BA"
            else -> {
                if (cleanName.length == 2) cleanName.uppercase() else "SP"
            }
        }
    }
}
