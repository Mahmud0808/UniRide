package com.drdisagree.uniride.ui.screens.driver.home.navdrawer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.EmojiTransportation
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Reviews
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.drdisagree.uniride.R
import com.drdisagree.uniride.data.events.Resource
import com.drdisagree.uniride.data.utils.Constant
import com.drdisagree.uniride.data.utils.Constant.DRIVER_COLLECTION
import com.drdisagree.uniride.data.utils.Constant.DRIVER_PRIVACY_POLICY_URL
import com.drdisagree.uniride.data.utils.Constant.PHONE_NUMBER_PREFIX
import com.drdisagree.uniride.data.utils.Constant.ROAD_TRANSPORT_ACT_URL
import com.drdisagree.uniride.data.utils.Prefs
import com.drdisagree.uniride.ui.screens.NavGraphs
import com.drdisagree.uniride.ui.screens.destinations.EditProfileScreenDestination
import com.drdisagree.uniride.ui.screens.destinations.HelpAndSupportScreenDestination
import com.drdisagree.uniride.ui.screens.destinations.MyReviewsScreenDestination
import com.drdisagree.uniride.ui.screens.destinations.OnBoardingScreenDestination
import com.drdisagree.uniride.ui.screens.driver.login.DriverLoginViewModel
import com.drdisagree.uniride.ui.theme.Dark
import com.drdisagree.uniride.ui.theme.Gray
import com.drdisagree.uniride.ui.theme.Gray15
import com.drdisagree.uniride.ui.theme.spacing
import com.drdisagree.uniride.utils.openUrl
import com.drdisagree.uniride.utils.switchLanguage
import com.drdisagree.uniride.viewmodels.GetDriverViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawer(
    navigator: DestinationsNavigator,
    drawerState: DrawerState,
    coroutineScope: CoroutineScope,
    getDriverViewModel: GetDriverViewModel = hiltViewModel(),
    driverLoginViewModel: DriverLoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    DrawerHeader(
        getDriverViewModel = getDriverViewModel
    )
    Spacer(
        modifier = Modifier
            .padding(
                start = MaterialTheme.spacing.medium1,
                end = MaterialTheme.spacing.medium1,
                bottom = MaterialTheme.spacing.medium1
            )
            .height(1.dp)
            .fillMaxWidth()
            .background(Gray15)
    )
    DrawerBody(
        items = listOf(
            MenuItemModel(
                id = "edit_profile",
                title = stringResource(R.string.edit_profile),
                contentDescription = stringResource(R.string.go_to_edit_profile_screen),
                icon = Icons.Outlined.Person,
                onClick = {
                    coroutineScope.launch {
                        drawerState.close()
                        navigator.navigate(EditProfileScreenDestination)
                    }
                }
            ),
            MenuItemModel(
                id = "reviews_about_me",
                title = stringResource(R.string.reviews_about_me),
                contentDescription = stringResource(R.string.check_reviews_about_me),
                icon = Icons.Outlined.Reviews,
                onClick = {
                    coroutineScope.launch {
                        drawerState.close()
                        navigator.navigate(MyReviewsScreenDestination)
                    }
                }
            ),
            MenuItemModel(
                id = "switch_language",
                title = stringResource(R.string.switch_language),
                contentDescription = stringResource(R.string.change_language_of_the_app),
                icon = Icons.Outlined.Language,
                onClick = {
                    coroutineScope.launch {
                        drawerState.close()
                        switchLanguage(context)
                    }
                }
            ),
            MenuItemModel(
                id = "help_and_support",
                title = stringResource(R.string.help_support),
                contentDescription = stringResource(R.string.get_help_and_support),
                icon = Icons.Outlined.Info,
                onClick = {
                    coroutineScope.launch {
                        drawerState.close()
                        navigator.navigate(
                            HelpAndSupportScreenDestination
                        ) {
                            launchSingleTop = true
                        }
                    }
                }
            ),
            MenuItemModel(
                id = "privacy_policy",
                title = stringResource(R.string.privacy_policy),
                contentDescription = stringResource(R.string.privacy_policy_for_drivers),
                icon = Icons.Outlined.Policy,
                onClick = {
                    coroutineScope.launch {
                        drawerState.close()
                        openUrl(context = context, url = DRIVER_PRIVACY_POLICY_URL)
                    }
                }
            ),
            MenuItemModel(
                id = "road_transport_act",
                title = stringResource(R.string.road_transport_act),
                contentDescription = stringResource(R.string.road_transport_act_2018),
                icon = Icons.Outlined.EmojiTransportation,
                onClick = {
                    coroutineScope.launch {
                        drawerState.close()
                        openUrl(context = context, url = ROAD_TRANSPORT_ACT_URL)
                    }
                }
            ),
            MenuItemModel(
                id = "sign_out",
                title = stringResource(R.string.sign_out),
                contentDescription = stringResource(R.string.sign_out_from_the_app),
                icon = Icons.AutoMirrored.Outlined.Logout,
                onClick = {
                    coroutineScope.launch {
                        drawerState.close()

                        driverLoginViewModel.signOut()

                        navigator.popBackStack()
                        navigator.navigate(
                            OnBoardingScreenDestination()
                        ) {
                            popUpTo(NavGraphs.root.startRoute)
                            launchSingleTop = true
                        }

                        Prefs.clearPref(Constant.WHICH_USER_COLLECTION)
                        Firebase.messaging.unsubscribeFromTopic(DRIVER_COLLECTION)
                    }
                }
            )
        )
    )
}

@Composable
private fun DrawerHeader(
    getDriverViewModel: GetDriverViewModel
) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf(context.getString(R.string.unknown)) }
    var phone by rememberSaveable { mutableStateOf<String?>(null) }
    var email by rememberSaveable { mutableStateOf<String?>(null) }
    var imageUrl by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(getDriverViewModel.getDriver) {
        getDriverViewModel.getDriver.collect { result ->
            when (result) {
                is Resource.Success -> {
                    name = result.data?.name ?: context.getString(R.string.unknown)
                    phone = result.data?.phone
                    email = result.data?.email
                    imageUrl = result.data?.profileImage
                }

                is Resource.Error -> {
                    Toast.makeText(
                        context,
                        result.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    Unit
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = MaterialTheme.spacing.extraLarge1,
                bottom = MaterialTheme.spacing.medium3,
                start = MaterialTheme.spacing.medium1,
                end = MaterialTheme.spacing.medium1
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val placeholder by remember { mutableIntStateOf(R.drawable.img_profile_pic_default) }

        val imageRequest = ImageRequest.Builder(context)
            .data(imageUrl)
            .dispatcher(Dispatchers.IO)
            .memoryCacheKey(imageUrl + "_low")
            .diskCacheKey(imageUrl + "_low")
            .placeholder(placeholder)
            .error(placeholder)
            .fallback(placeholder)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .crossfade(250)
            .size(256)
            .build()

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(100))
                .background(Gray)
                .padding(MaterialTheme.spacing.small1)
        ) {
            AsyncImage(
                model = imageRequest,
                placeholder = painterResource(id = R.drawable.img_loading),
                contentDescription = stringResource(R.string.profile_picture),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(100)),
                contentScale = ContentScale.Crop,
            )
        }

        val phoneNumber = if (phone?.startsWith(PHONE_NUMBER_PREFIX) == true) {
            phone
        } else {
            "$PHONE_NUMBER_PREFIX${phone?.drop(1)}"
        }

        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = MaterialTheme.spacing.small3)
        )
        Text(
            text = email ?: phoneNumber ?: "unknown@email.com",
            fontSize = 14.sp,
            lineHeight = 18.sp,
            color = Dark
        )
    }
}

@Composable
private fun DrawerBody(
    items: List<MenuItemModel>,
    modifier: Modifier = Modifier,
    itemTextStyle: TextStyle = TextStyle(fontSize = 16.sp)
) {
    LazyColumn(modifier) {
        items(
            count = items.size,
            key = { items[it].id }
        ) { item ->
            NavigationDrawerItem(
                label = {
                    Text(
                        text = items[item].title,
                        style = itemTextStyle,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                selected = false,
                onClick = {
                    items[item].onClick?.invoke()
                },
                icon = {
                    Icon(
                        imageVector = items[item].icon,
                        contentDescription = items[item].contentDescription
                    )
                },
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small2)
            )
        }
    }
}