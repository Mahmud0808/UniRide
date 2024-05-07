package com.drdisagree.uniride.ui.screens.student.mylocation

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@SuppressLint("MissingPermission")
class BusLocationViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    private val _locationFlow = MutableStateFlow<Location?>(null)
    val locationFlow: StateFlow<Location?> = _locationFlow.asStateFlow()

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    init {
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        viewModelScope.launch {
            callbackFlow {
                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        trySendBlocking(locationResult.lastLocation)
                    }
                }

                fusedLocationProviderClient.requestLocationUpdates(
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 50L)
                        .setWaitForAccurateLocation(false)
                        .setMinUpdateIntervalMillis(10L)
                        .setMaxUpdateDelayMillis(100L)
                        .build(),
                    locationCallback,
                    Looper.getMainLooper()
                )

                awaitClose {
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                }
            }.catch { e ->
                Log.e(
                    MyLocationViewModel::class.java.simpleName,
                    "Error getting location updates: $e"
                )
            }.shareIn(
                viewModelScope,
                replay = 0,
                started = SharingStarted.WhileSubscribed()
            ).collect { location ->
                _locationFlow.value = location
            }
        }
    }
}