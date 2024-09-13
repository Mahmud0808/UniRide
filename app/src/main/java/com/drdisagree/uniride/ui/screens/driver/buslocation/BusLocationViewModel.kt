package com.drdisagree.uniride.ui.screens.driver.buslocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.uniride.data.events.BusStatus
import com.drdisagree.uniride.data.events.Resource
import com.drdisagree.uniride.data.models.LatLngSerializable
import com.drdisagree.uniride.data.models.RunningBus
import com.drdisagree.uniride.data.utils.Constant.RUNNING_BUS_COLLECTION
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class BusLocationViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private var userId: String? = null
    private var busDocument: QuerySnapshot? = null

    private val _updateBusLocation = MutableSharedFlow<Resource<Unit>>()
    val updateBusLocation = _updateBusLocation.asSharedFlow()

    private val _updateBusStatus = MutableSharedFlow<Resource<Unit>>()
    val updateBusStatus = _updateBusStatus.asSharedFlow()

    private val locationUpdateQueue = Channel<LatLng>(Channel.UNLIMITED)
    private var isProcessingUpdates = false

    private val _runningBus = MutableStateFlow<RunningBus?>(null)
    val runningBus: StateFlow<RunningBus?> = _runningBus.asStateFlow()

    init {
        userId = firebaseAuth.currentUser?.uid

        fetchRunningBus()

        viewModelScope.launch {
            for (location in locationUpdateQueue) {
                processLocationUpdate(location)
            }
        }
    }

    private fun fetchRunningBus() {
        viewModelScope.launch {
            if (userId == null) {
                userId = firebaseAuth.currentUser?.uid

                if (userId == null) {
                    _updateBusStatus.emit(
                        Resource.Error("User not authenticated")
                    )
                    return@launch
                }
            }

            firestore.collection(RUNNING_BUS_COLLECTION)
                .whereEqualTo("driver.id", userId)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        _runningBus.value =
                            result.documents.first().toObject(RunningBus::class.java)
                    }
                }
                .addOnFailureListener { exception ->
                    viewModelScope.launch {
                        _updateBusStatus.emit(
                            Resource.Error(exception.message ?: "Error fetching running bus")
                        )
                    }
                }
        }
    }

    fun updateBusLocation(location: LatLng) {
        viewModelScope.launch {
            locationUpdateQueue.send(location)
        }
    }

    fun updateBusStatus(newStatus: BusStatus) {
        viewModelScope.launch {
            _updateBusStatus.emit(
                Resource.Loading()
            )

            if (userId == null) {
                userId = firebaseAuth.currentUser?.uid

                if (userId == null) {
                    _updateBusStatus.emit(
                        Resource.Error("User not authenticated")
                    )
                    return@launch
                }
            }

            busDocument = firestore.collection(RUNNING_BUS_COLLECTION)
                .whereEqualTo("driver.id", userId)
                .get()
                .await()

            if (busDocument == null || busDocument!!.isEmpty) {
                _updateBusStatus.emit(
                    Resource.Error("Bus not found for the current driver id $userId")
                )
                return@launch
            }

            val bus = busDocument!!.documents.first()
            val updatedBus = bus.toObject(RunningBus::class.java)?.copy(status = newStatus)

            if (updatedBus != null) {
                firestore.collection(RUNNING_BUS_COLLECTION)
                    .document(bus.id)
                    .set(updatedBus)
                    .addOnSuccessListener {
                        _runningBus.value = updatedBus

                        viewModelScope.launch {
                            _updateBusStatus.emit(
                                Resource.Success(Unit)
                            )
                        }
                    }
                    .addOnFailureListener {
                        viewModelScope.launch {
                            _updateBusStatus.emit(
                                Resource.Error("Failed to update bus status")
                            )
                        }
                    }
            } else {
                _updateBusStatus.emit(
                    Resource.Error("Failed to convert bus document to RunningBus")
                )
            }
        }
    }

    fun stopBus(
        onResult: (Boolean) -> Unit,
    ) {
        viewModelScope.launch {
            _updateBusStatus.emit(
                Resource.Loading()
            )

            if (userId == null) {
                userId = firebaseAuth.currentUser?.uid

                if (userId == null) {
                    _updateBusStatus.emit(
                        Resource.Error("User not authenticated")
                    )
                    onResult(false)
                    return@launch
                }
            }

            busDocument = firestore.collection(RUNNING_BUS_COLLECTION)
                .whereEqualTo("driver.id", userId)
                .get()
                .await()

            if (busDocument == null || busDocument!!.isEmpty) {
                _updateBusStatus.emit(
                    Resource.Error("Bus not found for the current driver id $userId")
                )
                onResult(false)
                return@launch
            }

            val bus = busDocument!!.documents.first()
            val updatedBus = bus.toObject(RunningBus::class.java)
                ?.copy(
                    driver = null,
                    status = BusStatus.STOPPED,
                    reachedAt = System.currentTimeMillis()
                )

            if (updatedBus != null) {
                firestore.collection(RUNNING_BUS_COLLECTION)
                    .document(bus.id)
                    .set(updatedBus)
                    .addOnSuccessListener {
                        _runningBus.value = updatedBus

                        viewModelScope.launch {
                            _updateBusStatus.emit(
                                Resource.Success(Unit)
                            )
                            onResult(true)
                        }
                    }
                    .addOnFailureListener {
                        viewModelScope.launch {
                            _updateBusStatus.emit(
                                Resource.Error("Failed to update bus status")
                            )
                            onResult(false)
                        }
                    }
            } else {
                _updateBusStatus.emit(
                    Resource.Error("Failed to convert bus document to RunningBus")
                )
                onResult(false)
            }
        }
    }

    private suspend fun processLocationUpdate(location: LatLng) {
        if (userId == null) {
            userId = firebaseAuth.currentUser?.uid

            if (userId == null) {
                viewModelScope.launch {
                    _updateBusLocation.emit(
                        Resource.Error("User not authenticated")
                    )
                }
                return
            }
        }

        busDocument = firestore.collection(RUNNING_BUS_COLLECTION)
            .whereEqualTo("driver.id", userId)
            .get()
            .await()

        if (busDocument == null || busDocument!!.isEmpty) {
            // Bus has been stopped
            return
        }

        if (isProcessingUpdates) return

        isProcessingUpdates = true

        try {
            val bus = busDocument!!.documents.first()
            val busObject = bus.toObject(RunningBus::class.java)
            val updatedBus = busObject?.copy(
                currentlyAt = LatLngSerializable.fromLatLng(location),
                status = _runningBus.value?.status ?: busObject.status
            )

            if (updatedBus != null) {
                firestore.collection(RUNNING_BUS_COLLECTION)
                    .document(bus.id)
                    .set(updatedBus)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            _updateBusLocation.emit(
                                Resource.Success(Unit)
                            )
                        }
                    }
                    .addOnFailureListener {
                        viewModelScope.launch {
                            if (_runningBus.value?.status != BusStatus.STOPPED) {
                                _updateBusLocation.emit(
                                    Resource.Error("Failed to update bus location")
                                )
                            }
                        }
                    }
            } else {
                _updateBusLocation.emit(
                    Resource.Error("Failed to convert bus document to RunningBus")
                )
            }
        } catch (e: Exception) {
            _updateBusLocation.emit(
                Resource.Error(e.message.toString())
            )
        } finally {
            isProcessingUpdates = false
        }
    }
}