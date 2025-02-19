package com.drdisagree.uniride.ui.screens.student.main

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.drdisagree.uniride.ui.components.navigation.BottomBarDestination
import com.drdisagree.uniride.ui.components.transitions.SlideInOutTransition
import com.drdisagree.uniride.ui.components.views.Container
import com.drdisagree.uniride.ui.components.views.NoInternetDialog
import com.drdisagree.uniride.ui.screens.NavGraphs
import com.drdisagree.uniride.ui.theme.Gray
import com.drdisagree.uniride.ui.theme.LightGray
import com.drdisagree.uniride.ui.theme.NoRippleTheme
import com.drdisagree.uniride.utils.SystemUtils.isInternetAvailable
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popBackStack
import com.ramcosta.composedestinations.rememberNavHostEngine
import kotlinx.coroutines.delay

private lateinit var rootNavigator: DestinationsNavigator

@RootNavGraph
@Destination(style = SlideInOutTransition::class)
@Composable
fun HomeContainer(
    navigator: DestinationsNavigator
) {
    rootNavigator = navigator

    val engine = rememberNavHostEngine()
    val navController = engine.rememberNavController()

    Container {
        Scaffold(
            bottomBar = {
                BottomNavBar(navController = navController)
            }
        ) { paddingValues ->
            DestinationsNavHost(
                engine = engine,
                navController = navController,
                navGraph = NavGraphs.bottom,
                startRoute = NavGraphs.bottom.startRoute,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomNavBar(
    navController: NavController
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.navigationBarColor = LightGray.toArgb()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    NavigationBar(
        containerColor = LightGray,
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp
                )
            )
            .background(color = Gray)
            .padding(top = 6.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp
                )
            )
    ) {
        BottomBarDestination.entries.forEach { destination ->
            val isCurrentDestOnBackStack =
                navBackStackEntry.isRouteOnBackStack(destination.graph.route)

            CompositionLocalProvider(LocalRippleConfiguration provides NoRippleTheme) {
                NavigationBarItem(
                    selected = isCurrentDestOnBackStack,
                    onClick = {
                        if (isCurrentDestOnBackStack) {
                            navController.popBackStack(destination.graph.startRoute, false)
                            return@NavigationBarItem
                        }

                        navController.navigate(destination.graph) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                                saveState = true
                            }

                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    icon = {
                        BadgedBox(badge = {
                            if (destination.badgeCount > 0) {
                                Badge {
                                    Text(text = destination.badgeCount.toString())
                                }
                            }
                        }) {
                            Icon(
                                painter = painterResource(
                                    id = if (isCurrentDestOnBackStack) {
                                        destination.selectedIcon
                                    } else {
                                        destination.unselectedIcon
                                    }
                                ),
                                contentDescription = stringResource(id = destination.label),
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(if (isCurrentDestOnBackStack) 0.dp else 1.dp)
                            )
                        }
                    },
                    label = {
                        Text(text = stringResource(id = destination.label))
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = Color.Black,
                        unselectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        unselectedTextColor = Color.Black
                    ),
                    alwaysShowLabel = true
                )
            }
        }
    }

    val context = LocalContext.current
    var isNoInternetDialogShown by rememberSaveable { mutableStateOf(false) }
    var isInternetAvailable by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            isInternetAvailable = isInternetAvailable(context)

            if (!isInternetAvailable && !isNoInternetDialogShown) {
                isNoInternetDialogShown = true
            } else if (isInternetAvailable && isNoInternetDialogShown) {
                isNoInternetDialogShown = false
            }
            delay(30000)
        }
    }

    if (isNoInternetDialogShown) {
        NoInternetDialog(
            context = context,
            onDismiss = { isNoInternetDialogShown = false }
        )
    }
}

private fun NavBackStackEntry?.isRouteOnBackStack(route: String): Boolean {
    return if (this == null) false else destination.hierarchy.any { it.route == route }
}

fun getRootNavigator(): DestinationsNavigator = rootNavigator