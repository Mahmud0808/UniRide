package com.drdisagree.uniride.ui.screens.admin.newroute

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import com.drdisagree.uniride.R
import com.drdisagree.uniride.data.events.Resource
import com.drdisagree.uniride.data.models.Route
import com.drdisagree.uniride.ui.components.navigation.MoreNavGraph
import com.drdisagree.uniride.ui.components.transitions.FadeInOutTransition
import com.drdisagree.uniride.ui.components.views.ButtonPrimary
import com.drdisagree.uniride.ui.components.views.Container
import com.drdisagree.uniride.ui.components.views.LoadingDialog
import com.drdisagree.uniride.ui.components.views.StyledTextField
import com.drdisagree.uniride.ui.components.views.TopAppBarWithBackButton
import com.drdisagree.uniride.ui.screens.admin.account.AccountStatusViewModel
import com.drdisagree.uniride.ui.theme.spacing
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@MoreNavGraph
@Destination(style = FadeInOutTransition::class)
@Composable
fun NewRoute(
    navigator: DestinationsNavigator
) {
    Container(shadow = false) {
        Scaffold(
            topBar = {
                TopAppBarWithBackButton(
                    title = stringResource(R.string.new_route),
                    onBackClick = {
                        navigator.navigateUp()
                    }
                )
            },
            content = { paddingValues ->
                NewRouteContent(
                    paddingValues = paddingValues,
                    navigator = navigator
                )
            }
        )
    }
}

@Composable
private fun NewRouteContent(
    paddingValues: PaddingValues,
    navigator: DestinationsNavigator,
    accountStatusViewModel: AccountStatusViewModel = hiltViewModel()
) {
    var isAdminState by remember { mutableStateOf<Boolean?>(null) }

    DisposableEffect(key1 = accountStatusViewModel.isAdmin) {
        val isAdminLiveData = accountStatusViewModel.isAdmin
        val observer = Observer<Boolean?> { isAdmin ->
            isAdminState = isAdmin
        }
        isAdminLiveData.observeForever(observer)

        onDispose {
            isAdminLiveData.removeObserver(observer)
        }
    }

    when (isAdminState) {
        null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .background(Color.White)
                        .wrapContentSize()
                )
            }
        }

        true -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(MaterialTheme.spacing.medium1)
            ) {
                NewRouteFields()
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "You are not an admin"
                )
            }
        }
    }
}

@Composable
private fun NewRouteFields(
    routeViewModel: NewRouteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var routeNo by rememberSaveable { mutableStateOf("") }
    var startTime by rememberSaveable { mutableStateOf("") }
    var routeName by rememberSaveable { mutableStateOf("") }
    var routeDetails by rememberSaveable { mutableStateOf("") }
    var departureTime by rememberSaveable { mutableStateOf("") }
    var routeMap by rememberSaveable { mutableStateOf("") }

    StyledTextField(
        placeholder = "Route No",
        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small2),
        onValueChange = { routeNo = it },
        inputText = routeNo,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )

    StyledTextField(
        placeholder = "Start Time (To DSC) (Separated by $$)",
        modifier = Modifier.padding(
            start = MaterialTheme.spacing.small2,
            end = MaterialTheme.spacing.small2,
            top = MaterialTheme.spacing.medium1
        ),
        onValueChange = { startTime = it },
        inputText = startTime,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = false
    )

    StyledTextField(
        placeholder = "Route Name",
        modifier = Modifier.padding(
            start = MaterialTheme.spacing.small2,
            end = MaterialTheme.spacing.small2,
            top = MaterialTheme.spacing.medium1
        ),
        onValueChange = { routeName = it },
        inputText = routeName,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )

    StyledTextField(
        placeholder = "Route Details (Locations separated by <>)",
        modifier = Modifier.padding(
            start = MaterialTheme.spacing.small2,
            end = MaterialTheme.spacing.small2,
            top = MaterialTheme.spacing.medium1
        ),
        onValueChange = { routeDetails = it },
        inputText = routeDetails,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = false
    )

    StyledTextField(
        placeholder = "Departure Time (From DSC) (Separated by $$)",
        modifier = Modifier.padding(
            start = MaterialTheme.spacing.small2,
            end = MaterialTheme.spacing.small2,
            top = MaterialTheme.spacing.medium1
        ),
        onValueChange = { departureTime = it },
        inputText = departureTime,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = false
    )

    StyledTextField(
        placeholder = "Route Map (Google Maps URL)",
        modifier = Modifier.padding(
            start = MaterialTheme.spacing.small2,
            end = MaterialTheme.spacing.small2,
            top = MaterialTheme.spacing.medium1
        ),
        onValueChange = { routeMap = it },
        inputText = routeMap,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )

    ButtonPrimary(
        modifier = Modifier
            .padding(
                start = MaterialTheme.spacing.small2,
                end = MaterialTheme.spacing.small2,
                top = MaterialTheme.spacing.medium1,
                bottom = MaterialTheme.spacing.medium1
            )
            .fillMaxWidth(),
        text = "Submit"
    ) {
        if (
            routeNo.isEmpty() ||
            startTime.isEmpty() ||
            routeName.isEmpty() ||
            routeDetails.isEmpty() ||
            departureTime.isEmpty()
        ) {
            Toast.makeText(
                context,
                "Please fill in all fields",
                Toast.LENGTH_SHORT
            ).show()

            return@ButtonPrimary
        }

        routeViewModel.saveRoute(
            Route(
                routeNo = routeNo,
                startTime = startTime,
                routeName = routeName,
                routeDetails = routeDetails,
                departureTime = departureTime,
                routeWebUrl = routeMap.ifEmpty { null }
            )
        )
    }

    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        routeViewModel.state.collect { result ->
            when (result) {
                is Resource.Loading -> {
                    showLoadingDialog = true
                }

                is Resource.Success -> {
                    showLoadingDialog = false

                    routeNo = ""
                    startTime = ""
                    routeName = ""
                    routeDetails = ""
                    departureTime = ""
                    routeMap = ""

                    Toast.makeText(
                        context,
                        result.data,
                        Toast.LENGTH_SHORT
                    ).show()
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