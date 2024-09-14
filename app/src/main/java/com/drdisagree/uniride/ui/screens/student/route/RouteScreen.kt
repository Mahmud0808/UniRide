package com.drdisagree.uniride.ui.screens.student.route

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.drdisagree.uniride.R
import com.drdisagree.uniride.data.events.Resource
import com.drdisagree.uniride.data.models.Route
import com.drdisagree.uniride.ui.components.navigation.RoutesNavGraph
import com.drdisagree.uniride.ui.components.transitions.FadeInOutTransition
import com.drdisagree.uniride.ui.components.views.Container
import com.drdisagree.uniride.ui.components.views.TopAppBarWithBackButton
import com.drdisagree.uniride.ui.screens.destinations.RouteDetailsScreenDestination
import com.drdisagree.uniride.ui.theme.Dark
import com.drdisagree.uniride.ui.theme.LightGray
import com.drdisagree.uniride.ui.theme.spacing
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.util.Locale

@RoutesNavGraph(start = true)
@Destination(style = FadeInOutTransition::class)
@Composable
fun RouteScreen(
    navigator: DestinationsNavigator
) {
    Container(shadow = false) {
        Scaffold(
            topBar = {
                TopAppBarWithBackButton(
                    title = stringResource(id = R.string.nav_routes),
                    onBackClick = {
                        navigator.navigateUp()
                    }
                )
            },
            content = { paddingValues ->
                RouteContent(
                    navigator = navigator,
                    paddingValues = paddingValues
                )
            }
        )
    }
}

@Composable
private fun RouteContent(
    navigator: DestinationsNavigator,
    paddingValues: PaddingValues,
    routeViewModel: RouteViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val context = LocalContext.current
        var showLoadingDialog by rememberSaveable { mutableStateOf(false) }
        val routes by routeViewModel.routes.collectAsState(initial = Resource.Unspecified())

        when (routes) {
            is Resource.Loading -> {
                showLoadingDialog = true
            }

            is Resource.Success -> {
                showLoadingDialog = false

                val routeList = (routes as Resource.Success<List<Route>>).data

                if (routeList?.isNotEmpty() == true) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues = paddingValues)
                    ) {
                        items(
                            count = routeList.size,
                            key = { route -> routeList[route].uuid }
                        ) { route ->
                            RoutesListItem(
                                index = routeList[route].timeStamp.toInt(),
                                route = routeList[route],
                                onClick = {
                                    navigator.navigate(
                                        RouteDetailsScreenDestination(
                                            routeList[route]
                                        )
                                    )
                                }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Route is empty!"
                        )
                    }
                }
            }

            is Resource.Error -> {
                showLoadingDialog = false

                Toast.makeText(
                    context,
                    (routes as Resource.Error<List<Route>>).message,
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                Unit
            }
        }

        if (showLoadingDialog) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun RoutesListItem(
    modifier: Modifier = Modifier,
    index: Int,
    route: Route,
    onClick: (() -> Unit)? = null
) {
    val categoryLowercase = route.routeCategory.name.lowercase(Locale.getDefault())
    val categoryPillBackgroundColor = if (categoryLowercase.contains("shuttle")) {
        Color(0xFFE9FAF4)
    } else if (categoryLowercase.contains("friday")) {
        Color(0xFFFBEBEC)
    } else { // regular
        Color(0xFFF0F0F2)
    }
    val categoryPillTextColor = if (categoryLowercase.contains("shuttle")) {
        Color(0xFF0B710A)
    } else if (categoryLowercase.contains("friday")) {
        Color(0xFF881418)
    } else { // regular
        Dark
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                onClick?.invoke()
            }
    ) {
        if (index != 0) {
            HorizontalDivider(
                color = LightGray,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium1)
            )
        }

        Row(
            modifier = modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.medium3,
                    vertical = MaterialTheme.spacing.medium1
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.align(Alignment.Top)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_map_with_marker),
                    contentDescription = "Map with marker image",
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(28.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = MaterialTheme.spacing.medium1),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Route ${route.routeNo}",
                        fontSize = 16.sp,
                        style = TextStyle(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = MaterialTheme.spacing.small1)
                            .clip(RoundedCornerShape(28.dp))
                            .background(categoryPillBackgroundColor)
                            .padding(
                                horizontal = MaterialTheme.spacing.small2,
                                vertical = MaterialTheme.spacing.extraSmall1
                            )
                    ) {
                        Text(
                            text = route.routeCategory.name,
                            color = categoryPillTextColor,
                            fontSize = 14.sp
                        )
                    }
                }
                Text(
                    text = route.routeName,
                    color = Dark,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "View details",
                tint = Color.Black,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(LightGray)
                    .padding(8.dp)
                    .size(16.dp)
            )
        }
    }
}