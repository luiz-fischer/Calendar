package com.example.calendar

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.calendar.ui.CalendarScreen
import com.example.calendar.ui.CalendarViewModel
import com.example.calendar.ui.theme.CalendarTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: CalendarViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarTheme {
                val locationPermissionState = rememberPermissionState(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                LaunchedEffect(locationPermissionState.status.isGranted) {
                    if (locationPermissionState.status.isGranted) {
                        fetchLocation()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalendarScreen(
                        viewModel = viewModel,
                        onRequestLocation = {
                            if (locationPermissionState.status.isGranted) {
                                fetchLocation()
                            } else {
                                locationPermissionState.launchPermissionRequest()
                            }
                        }
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Tenta obter a última localização conhecida primeiro
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                processLocation(location.latitude, location.longitude)
            } else {
                // Se não houver última localização, solicita uma atualização atual
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { currentLocation ->
                        currentLocation?.let {
                            processLocation(it.latitude, it.longitude)
                        }
                    }
            }
        }.addOnFailureListener {
            Log.e("MainActivity", "Erro ao obter localização", it)
        }
    }

    private fun processLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale("pt", "BR"))
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val stateCode = stateToUF(address.adminArea)
                        viewModel.onLocationDetected(stateCode, address.locality ?: "")
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val stateCode = stateToUF(address.adminArea)
                    viewModel.onLocationDetected(stateCode, address.locality ?: "")
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro no Geocoder", e)
        }
    }

    private fun stateToUF(stateName: String?): String {
        val cleanName = stateName?.lowercase()?.trim() ?: ""
        return when (cleanName) {
            "acre" -> "AC"
            "alagoas" -> "AL"
            "amapá", "amapa" -> "AP"
            "amazonas" -> "AM"
            "bahia" -> "BA"
            "ceará", "ceara" -> "CE"
            "distrito federal" -> "DF"
            "espírito santo", "espirito santo" -> "ES"
            "goiás", "goias" -> "GO"
            "maranhão", "maranhao" -> "MA"
            "mato grosso" -> "MT"
            "mato grosso do sul" -> "MS"
            "minas gerais" -> "MG"
            "pará", "para" -> "PA"
            "paraíba", "paraiba" -> "PB"
            "paraná", "parana" -> "PR"
            "pernambuco" -> "PE"
            "piauí", "piaui" -> "PI"
            "rio de janeiro" -> "RJ"
            "rio grande do norte" -> "RN"
            "rio grande do sul" -> "RS"
            "rondônia", "rondonia" -> "RO"
            "roraima" -> "RR"
            "santa catarina" -> "SC"
            "são paulo", "sao paulo" -> "SP"
            "sergipe" -> "SE"
            "tocantins" -> "TO"
            else -> if (cleanName.length == 2) cleanName.uppercase() else "SP"
        }
    }
}
