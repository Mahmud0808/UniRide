package com.drdisagree.uniride.ui.screens.driver.buslocation

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.drdisagree.uniride.R
import com.drdisagree.uniride.data.enums.BusStatus
import com.drdisagree.uniride.data.events.Resource
import com.drdisagree.uniride.services.LocationService
import com.drdisagree.uniride.ui.components.transitions.SlideInOutTransition
import com.drdisagree.uniride.ui.components.views.ButtonPrimary
import com.drdisagree.uniride.ui.components.views.ButtonSecondary
import com.drdisagree.uniride.ui.components.views.Container
import com.drdisagree.uniride.ui.components.views.DisableBackHandler
import com.drdisagree.uniride.ui.components.views.KeepScreenOn
import com.drdisagree.uniride.ui.components.views.LoadingDialog
import com.drdisagree.uniride.ui.components.views.NoInternetDialog
import com.drdisagree.uniride.ui.components.views.RequestGpsEnable
import com.drdisagree.uniride.ui.components.views.StyledAlertDialog
import com.drdisagree.uniride.ui.components.views.TopAppBarNoButton
import com.drdisagree.uniride.ui.components.views.areLocationPermissionsGranted
import com.drdisagree.uniride.ui.components.views.isGpsEnabled
import com.drdisagree.uniride.ui.theme.spacing
import com.drdisagree.uniride.utils.AnimationQueue
import com.drdisagree.uniride.utils.MathUtils
import com.drdisagree.uniride.utils.SystemUtils.isInternetAvailable
import com.drdisagree.uniride.utils.sensorRotationEffect
import com.drdisagree.uniride.utils.toBitmapDescriptor
import com.drdisagree.uniride.viewmodels.LocationSharingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlin.math.abs

@RootNavGraph
@Destination(style = SlideInOutTransition::class)
@Composable
fun BusLocation(
    navigator: DestinationsNavigator
) {
    DisableBackHandler(isDisabled = true) {
        Container(
            modifier = Modifier.background(Color(0xFFF2F2F2)),
            shadow = false
        ) {
            Scaffold(
                topBar = {
                    TopAppBarNoButton(
                        title = stringResource(R.string.sharing_location)
                    )
                },
                content = { paddingValues ->
                    MapView(
                        navigator = navigator,
                        paddingValues = paddingValues
                    )
                }
            )
        }
    }
}

@Composable
private fun MapView(
    navigator: DestinationsNavigator,
    paddingValues: PaddingValues,
    locationViewModel: LocationSharingViewModel = hiltViewModel(),
    busLocationViewModel: BusLocationViewModel = hiltViewModel()
) {
    KeepScreenOn()
    CheckGpsAndInternetPeriodically()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Intent(context.applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            context.startService(this)
        }
    }

    var isMapLoaded by remember { mutableStateOf(false) }
    var marker: LatLng? by rememberSaveable { mutableStateOf(null) }
    val myLocation by locationViewModel.locationFlow.collectAsState()
    val routePoints by busLocationViewModel.routePoints.observeAsState(emptyList())
    val destinationPlace by busLocationViewModel.destination.observeAsState(null)
    myLocation?.let {
        if (marker == null || marker != LatLng(it.latitude, it.longitude)) {
            marker = LatLng(it.latitude, it.longitude)
        }
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(23.8161532, 90.2747436), 15f)
    }
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = false,
            myLocationButtonEnabled = false
        )
    }
    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = false,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )
        )
    }

    val degrees = sensorRotationEffect(context)
    var zoomLevel by rememberSaveable { mutableFloatStateOf(15f) }

    LaunchedEffect(
        key1 = true
    ) {
        val locationPermissionGranted = areLocationPermissionsGranted(context)
        val locationEnabled = isGpsEnabled(context)

        if (!locationPermissionGranted) {
            Toast.makeText(
                context,
                "Location permission not granted",
                Toast.LENGTH_SHORT
            ).show()
        }
        if (!locationEnabled) {
            Toast.makeText(
                context,
                "GPS is not enabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(key1 = marker) {
        marker?.let {
            busLocationViewModel.updateBusLocation(location = it)
        }
    }

    LaunchedEffect(key1 = marker, key2 = degrees) {
        marker?.let {
            val cameraPosition = CameraPosition.Builder()
                .target(
                    LatLng(
                        it.latitude,
                        it.longitude
                    )
                )
                .zoom(zoomLevel)
                .bearing(degrees.toFloat())
                .build()

            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(cameraPosition),
                1_000
            )
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { cameraPositionState.position.zoom }
            .collect { zoom ->
                if (zoomLevel != zoom) {
                    zoomLevel = zoom
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = paddingValues)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),

            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = mapProperties,
            onMapLoaded = {
                isMapLoaded = true
            }
        ) {
            var previousLatLng by remember { mutableStateOf<LatLng?>(null) }
            var previousRotation by remember { mutableDoubleStateOf(0.0) }
            var vehicleHasMoved by remember { mutableStateOf(false) }
            val thresholdLatLng = 0.00001

            if (marker != null) {
                val scope = rememberCoroutineScope()
                val markerState = rememberMarkerState(position = marker!!)
                val updatePosition = { pos: LatLng -> markerState.position = pos }
                val animationQueue = AnimationQueue(markerState.position, scope, updatePosition)

                LaunchedEffect(marker) {
                    animationQueue.addToQueue(marker!!, 0f)
                }

                val rotationAngle = if (previousLatLng != null &&
                    previousLatLng != marker &&
                    (abs(previousLatLng!!.latitude - marker!!.latitude) > thresholdLatLng ||
                            abs(previousLatLng!!.longitude - marker!!.longitude) > thresholdLatLng)
                ) {
                    val newRotation = MathUtils.calculateDegrees(previousLatLng!!, marker!!)
                    previousRotation = newRotation
                    vehicleHasMoved = true
                    newRotation
                } else {
                    previousRotation
                }
                LaunchedEffect(marker) {
                    previousLatLng = marker
                }

                val busMarkerBitmap = remember(vehicleHasMoved) {
                    if (!vehicleHasMoved) {
                        toBitmapDescriptor(context, R.drawable.ic_pin_map_bus)
                    } else {
                        toBitmapDescriptor(context, R.drawable.img_bus_top_view, 16.dp)
                    }
                }

                Marker(
                    state = markerState,
                    title = stringResource(R.string.me),
                    snippet = stringResource(R.string.my_position),
                    draggable = false,
                    icon = busMarkerBitmap,
                    rotation = rotationAngle.toFloat(),
                    anchor = Offset(0.5f, 0.5f)
                )
            }
            if (routePoints.isNotEmpty()) {
                Polyline(
                    points = routePoints,
                    color = Color.Blue,
                    width = 10f
                )
            }
        }

        var openDialog by remember { mutableStateOf(false) }
        val runningBus by busLocationViewModel.runningBus.collectAsState()
        val status = runningBus?.status ?: BusStatus.STANDBY
        var showLoadingDialog by rememberSaveable { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            ButtonSecondary(
                modifier = Modifier
                    .padding(
                        start = MaterialTheme.spacing.medium1,
                        end = MaterialTheme.spacing.medium1,
                        top = MaterialTheme.spacing.medium1,
                    )
                    .fillMaxWidth(),
                text = when (runningBus?.busFull ?: false) {
                    true -> stringResource(R.string.occupancy_all_seats_occupied)
                    false -> stringResource(R.string.occupancy_few_seats_available)
                }
            ) {
                when (runningBus?.busFull ?: false) {
                    true -> {
                        busLocationViewModel.updateBusStatus(busOccupied = false)
                    }

                    false -> {
                        busLocationViewModel.updateBusStatus(busOccupied = true)
                    }
                }
            }
            ButtonPrimary(
                modifier = Modifier
                    .padding(
                        start = MaterialTheme.spacing.medium1,
                        end = MaterialTheme.spacing.medium1,
                        top = MaterialTheme.spacing.medium1,
                        bottom = MaterialTheme.spacing.extraLarge1
                    )
                    .fillMaxWidth(),
                text = when (status) {
                    BusStatus.STANDBY -> stringResource(R.string.update_status_to_driving)
                    BusStatus.RUNNING -> stringResource(R.string.stop_sharing_location)
                    BusStatus.STOPPED -> stringResource(R.string.start_sharing_location)
                }
            ) {
                when (status) {
                    BusStatus.STANDBY -> {
                        busLocationViewModel.updateBusStatus(newStatus = BusStatus.RUNNING)
                    }

                    BusStatus.RUNNING -> {
                        openDialog = true
                    }

                    BusStatus.STOPPED -> {
                        busLocationViewModel.updateBusStatus(newStatus = BusStatus.STANDBY)
                    }
                }
            }
        }

        if (openDialog) {
            StyledAlertDialog(
                title = stringResource(R.string.are_you_sure),
                message = stringResource(R.string.stop_sharing_location_confirmation),
                confirmButtonText = stringResource(R.string.stop),
                dismissButtonText = stringResource(R.string.cancel),
                onConfirmButtonClick = {
                    openDialog = false
                    busLocationViewModel.stopBus { success ->
                        if (success) {
                            Intent(
                                context.applicationContext,
                                LocationService::class.java
                            ).apply {
                                action = LocationService.ACTION_STOP
                                context.startService(this)
                            }

                            navigator.navigateUp()
                        }
                    }
                },
                onDismissButtonClick = {
                    openDialog = false
                },
                onDismissRequest = {
                    openDialog = false
                }
            )
        }

        LaunchedEffect(isMapLoaded, marker, destinationPlace) {
            val destinationLatLng = destinationPlace?.data?.latlng?.toLatLng()

            if (isMapLoaded && marker != null && destinationLatLng != null) {
                busLocationViewModel.fetchRoute(
                    origin = marker!!,
                    destination = destinationLatLng
                )
            }
        }

        LaunchedEffect(busLocationViewModel.updateBusStatus) {
            busLocationViewModel.updateBusStatus.collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        showLoadingDialog = true
                    }

                    is Resource.Success -> {
                        showLoadingDialog = false
                    }

                    is Resource.Error -> {
                        showLoadingDialog = false

                        Toast.makeText(
                            context,
                            result.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        showLoadingDialog = false
                    }
                }
            }
        }

        LaunchedEffect(busLocationViewModel.updateSeatOccupancy) {
            busLocationViewModel.updateSeatOccupancy.collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        showLoadingDialog = true
                    }

                    is Resource.Success -> {
                        showLoadingDialog = false
                    }

                    is Resource.Error -> {
                        showLoadingDialog = false

                        Toast.makeText(
                            context,
                            result.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        showLoadingDialog = false
                    }
                }
            }
        }

        if (showLoadingDialog) {
            LoadingDialog()
        }
    }

    if (!isMapLoaded) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = !isMapLoaded,
            enter = EnterTransition.None,
            exit = fadeOut()
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .background(Color.White)
                    .wrapContentSize()
            )
        }
    }

    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(busLocationViewModel.updateBusStatus) {
        busLocationViewModel.updateBusStatus.collect { result ->
            when (result) {
                is Resource.Loading -> {
                    showLoadingDialog = true
                }

                is Resource.Success -> {
                    showLoadingDialog = false
                }

                is Resource.Error -> {
                    showLoadingDialog = false

                    Toast.makeText(
                        context,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {
                    showLoadingDialog = false
                }
            }
        }
    }

    if (showLoadingDialog) {
        LoadingDialog()
    }

    LaunchedEffect(busLocationViewModel.updateBusLocation) {
        busLocationViewModel.updateBusLocation.collect { result ->
            when (result) {
                is Resource.Error -> {
                    Toast.makeText(
                        context,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {}
            }
        }
    }
}

@Composable
fun CheckGpsAndInternetPeriodically() {
    val context = LocalContext.current
    var requestGps by remember { mutableStateOf(false) }

    var isNoInternetDialogShown by rememberSaveable { mutableStateOf(false) }
    var isInternetAvailable by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            requestGps = !isGpsEnabled(context)
            isInternetAvailable = isInternetAvailable(context)

            if (!isInternetAvailable && !isNoInternetDialogShown) {
                isNoInternetDialogShown = true
            } else if (isInternetAvailable && isNoInternetDialogShown) {
                isNoInternetDialogShown = false
            }

            delay(5000)
        }
    }

    if (requestGps) {
        StyledAlertDialog(
            title = stringResource(R.string.gps_not_enabled),
            message = stringResource(R.string.enable_gps_to_share_location),
        )

        RequestGpsEnable(
            context = context,
            onGpsEnabled = {
                requestGps = false
            },
            onGpsDisabled = {
                Toast.makeText(
                    context,
                    "Please enable GPS",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    if (isNoInternetDialogShown) {
        NoInternetDialog(
            context = context,
            onDismiss = { isNoInternetDialogShown = false }
        )
    }
}