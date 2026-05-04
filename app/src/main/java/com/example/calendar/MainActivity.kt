package com.example.calendar

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
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
                        onRequestLocation = { locationPermissionState.launchPermissionRequest() }
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val geocoder = Geocoder(this, Locale("pt", "BR"))
                try {
                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val state = address.adminArea // Usually State Name or Code
                        val city = address.locality
                        
                        // Map state name to UF if necessary, or just use code if returned.
                        // For simplicity, we'll try to extract the UF from adminArea or subAdminArea.
                        val stateCode = stateToUF(state)
                        viewModel.onLocationDetected(stateCode, city ?: "")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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
