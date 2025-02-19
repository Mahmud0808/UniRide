package com.drdisagree.uniride.ui.screens.student.home

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.uniride.data.api.DirectionsApi
import com.drdisagree.uniride.data.api.Keys
import com.drdisagree.uniride.data.enums.BusStatus
import com.drdisagree.uniride.data.events.Resource
import com.drdisagree.uniride.data.models.RunningBus
import com.drdisagree.uniride.data.utils.Constant.RUNNING_BUS_COLLECTION
import com.drdisagree.uniride.utils.DistanceUtils.distance
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class NearbyBusesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val directionsApi: DirectionsApi
) : ViewModel() {

    private val _state = MutableSharedFlow<Resource<Unit>>()
    val state = _state.asSharedFlow()

    private val _runningBuses = MutableLiveData<List<RunningBus>>()
    val runningBuses: LiveData<List<RunningBus>> = _runningBuses

    private val _distances = MutableLiveData<Map<String, Double>>()
    val distances: LiveData<Map<String, Double>> = _distances

    private val lastKnownDistances = mutableMapOf<String, Double>()
    private var listenerRegistration: ListenerRegistration? = null

    init {
        startListening()
    }

    private fun startListening() {
        if (auth.currentUser == null) {
            _runningBuses.postValue(emptyList())
        }

        viewModelScope.launch {
            _state.emit(
                Resource.Loading()
            )
        }

        listenerRegistration = firestore.collection(RUNNING_BUS_COLLECTION)
            .whereNotEqualTo("status", BusStatus.STOPPED)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    _runningBuses.postValue(emptyList())
                    viewModelScope.launch {
                        _state.emit(
                            Resource.Error(exception.message.toString())
                        )
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val buses = snapshot.documents.mapNotNull { document ->
                        document.toObject(RunningBus::class.java)
                    }.filter { runningBus ->
                        runningBus.driver != null
                    }

                    _runningBuses.postValue(buses)

                    viewModelScope.launch {
                        delay(500)
                        _state.emit(
                            Resource.Success(Unit)
                        )
                    }
                } else {
                    _runningBuses.postValue(emptyList())
                }
            }
    }

    fun fetchAndStoreDistances(location: Location?, buses: List<RunningBus>) {
        location?.let { loc ->
            viewModelScope.launch {
                val distanceMap = mutableMapOf<String, Double>()

                buses.forEach { bus ->
                    bus.currentlyAt?.let { busLatLng ->
                        val distanceInKm = distance(
                            loc.latitude, loc.longitude,
                            busLatLng.latitude, busLatLng.longitude
                        )
                        val lastKnownDistance = lastKnownDistances[bus.uuid] ?: Double.MAX_VALUE

                        if (abs(distanceInKm * 1000 - lastKnownDistance) >= 100) {
                            fetchDistance(
                                LatLng(loc.latitude, loc.longitude),
                                LatLng(busLatLng.latitude, busLatLng.longitude)
                            ).onSuccess { distanceInMeters ->
                                distanceMap[bus.uuid] = distanceInMeters / 1000.0
                                lastKnownDistances[bus.uuid] = distanceInKm * 1000
                            }.onFailure { exception ->
                                Log.e(
                                    "NearbyBusesViewModel",
                                    "Error fetching distance: ${exception.message}"
                                )
                            }
                        } else {
                            _distances.value?.get(bus.uuid)?.let { distanceMap[bus.uuid] = it }
                        }
                    }
                }

                _distances.value = distanceMap
            }
        }
    }

    private suspend fun fetchDistance(origin: LatLng, destination: LatLng): Result<Int> {
        return try {
            val response = directionsApi.getDirections(
                origin = "${origin.latitude},${origin.longitude}",
                destination = "${destination.latitude},${destination.longitude}",
                apiKey = Keys.mapsApiKey()
            )

            if (response.routes.isNotEmpty()) {
                val distanceValue = response.routes.first().legs.first().distance.value
                Result.success(distanceValue)
            } else {
                Result.failure(Exception("No route found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}