package com.drdisagree.uniride.ui.screens.student.more

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.drdisagree.uniride.R
import com.drdisagree.uniride.data.models.Student
import com.drdisagree.uniride.data.utils.Constant.WHICH_USER_COLLECTION
import com.drdisagree.uniride.data.utils.Prefs
import com.drdisagree.uniride.ui.components.navigation.MoreNavGraph
import com.drdisagree.uniride.ui.components.transitions.FadeInOutTransition
import com.drdisagree.uniride.ui.components.views.Container
import com.drdisagree.uniride.ui.components.views.TopAppBarWithBackButtonAndEndIcon
import com.drdisagree.uniride.ui.screens.NavGraphs
import com.drdisagree.uniride.ui.screens.destinations.AdminPanelDestination
import com.drdisagree.uniride.ui.screens.destinations.MyLocationDestination
import com.drdisagree.uniride.ui.screens.destinations.OnBoardingScreenDestination
import com.drdisagree.uniride.ui.screens.student.account.StudentSignInViewModel
import com.drdisagree.uniride.ui.screens.student.main.getRootNavigator
import com.drdisagree.uniride.ui.theme.Dark
import com.drdisagree.uniride.ui.theme.Gray
import com.drdisagree.uniride.ui.theme.spacing
import com.drdisagree.uniride.utils.viewmodels.AccountStatusViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@MoreNavGraph(start = true)
@Destination(style = FadeInOutTransition::class)
@Composable
fun MoreScreen(
    navigator: DestinationsNavigator,
    studentSignInViewModel: StudentSignInViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    Container(shadow = false) {
        Scaffold(
            topBar = {
                TopAppBarWithBackButtonAndEndIcon(
                    title = stringResource(id = R.string.nav_more),
                    onBackClick = {
                        navigator.navigateUp()
                    },
                    endIcon = {
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logout),
                                contentDescription = "Logout",
                                tint = Color.Black.copy(alpha = 0.8f)
                            )
                        }
                    },
                    endIconClick = {
                        scope.launch {
                            studentSignInViewModel.signOut()

                            navigator.popBackStack()
                            getRootNavigator().popBackStack()
                            getRootNavigator().navigate(
                                OnBoardingScreenDestination()
                            ) {
                                popUpTo(NavGraphs.root.startRoute)
                                launchSingleTop = true
                            }
                            Prefs.clearPref(WHICH_USER_COLLECTION)
                        }
                    }
                )
            },
            content = { paddingValues ->
                MoreContent(
                    paddingValues = paddingValues,
                    navigator = navigator,
                    student = studentSignInViewModel.getSignedInStudent()
                )
            }
        )
    }
}

@Composable
private fun MoreContent(
    paddingValues: PaddingValues,
    navigator: DestinationsNavigator,
    student: Student
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileSection(student = student)
        QuickActionsSection(navigator = navigator)
    }
}

@Composable
private fun ProfileSection(
    student: Student
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.medium1),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val placeholder = R.drawable.img_profile_pic_default
        val imageUrl = student.profilePictureUrl

        val imageRequest = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .dispatcher(Dispatchers.IO)
            .memoryCacheKey(imageUrl)
            .diskCacheKey(imageUrl)
            .placeholder(placeholder)
            .error(placeholder)
            .fallback(placeholder)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(100))
                .background(Gray)
                .padding(MaterialTheme.spacing.small2)
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(100)),
                contentScale = ContentScale.Crop,
            )
        }

        Text(
            text = student.userName ?: "Unknown",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = MaterialTheme.spacing.small2)
        )
        Text(
            text = Firebase.auth.currentUser?.email ?: "unknown@diu.edu.bd",
            fontSize = 14.sp,
            lineHeight = 18.sp,
            color = Dark
        )
    }
}

@Composable
private fun QuickActionsSection(
    navigator: DestinationsNavigator,
    accountStatusViewModel: AccountStatusViewModel = hiltViewModel()
) {
    Row(
        modifier = Modifier.padding(
            top = MaterialTheme.spacing.medium1,
            bottom = MaterialTheme.spacing.medium1
        )
    ) {
        QuickActionsItem(
            icon = R.drawable.ic_map,
            title = R.string.my_location_title,
            subtitle = R.string.my_location_summary,
            modifier = Modifier
                .padding(start = MaterialTheme.spacing.medium1)
                .weight(1f),
            backgroundColor = Color(0xFFFFEDDE),
            backgroundWaveColor = Color(0xFFFFDEC4),
            iconBackgroundColor = Color(0xFFFFD6B7),
            onClick = {
                navigator.navigate(MyLocationDestination)
            }
        )

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium1))

        QuickActionsItem(
            icon = R.drawable.ic_warning,
            title = R.string.emergency_title,
            subtitle = R.string.emergency_summary,
            modifier = Modifier
                .padding(end = MaterialTheme.spacing.medium1)
                .weight(1f),
            backgroundColor = Color(0xFFEBEBFF),
            backgroundWaveColor = Color(0xFFDFDEFC),
            iconBackgroundColor = Color(0xFFD7D5FC),
        )
    }

    Row(
        modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium1)
    ) {
        QuickActionsItem(
            icon = R.drawable.ic_message,
            title = R.string.chat_box_title,
            subtitle = R.string.chat_box_summary,
            modifier = Modifier
                .padding(start = MaterialTheme.spacing.medium1)
                .weight(1f),
            backgroundColor = Color(0xFFDBE8E6),
            backgroundWaveColor = Color(0xFFCCD9D8),
            iconBackgroundColor = Color(0xFFBAD9D6),
        )

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium1))

        QuickActionsItem(
            icon = R.drawable.ic_sms_edit,
            title = R.string.report_title,
            subtitle = R.string.report_summary,
            modifier = Modifier
                .padding(end = MaterialTheme.spacing.medium1)
                .weight(1f),
            backgroundColor = Color(0xFFEEE6E2),
            backgroundWaveColor = Color(0xFFE0D5D0),
            iconBackgroundColor = Color(0xFFDECBC3),
        )
    }

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

    if (isAdminState == true) {
        Row(
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium1)
        ) {
            QuickActionsItem(
                icon = R.drawable.ic_category,
                title = R.string.admin_panel_title,
                subtitle = R.string.admin_panel_summary,
                modifier = Modifier
                    .padding(start = MaterialTheme.spacing.medium1)
                    .weight(1f),
                backgroundColor = Color(0xFFFFD6D6),
                backgroundWaveColor = Color(0xFFF8C4C4),
                iconBackgroundColor = Color(0xFFFABABA),
                onClick = {
                    navigator.navigate(AdminPanelDestination)
                }
            )

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium1))

            QuickActionsItem(
                icon = R.drawable.ic_sms_edit,
                title = R.string.report_title,
                subtitle = R.string.report_summary,
                modifier = Modifier
                    .padding(end = MaterialTheme.spacing.medium1)
                    .weight(1f)
                    .alpha(0f), // TODO: Reveal in future when new item needed
                backgroundColor = Color(0xFFFFDDC1),
                backgroundWaveColor = Color(0xFFF6D5BC),
                iconBackgroundColor = Color(0xFFFFCEA8),
            )
        }
    }
}

@Composable
private fun QuickActionsItem(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes subtitle: Int,
    backgroundColor: Color,
    backgroundWaveColor: Color,
    iconBackgroundColor: Color,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .heightIn(min = 160.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(backgroundColor)
            .clickable { onClick?.invoke() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_intersecting_waves_split),
            colorFilter = ColorFilter.tint(color = backgroundWaveColor),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.extraLarge)
        )
        Column(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium1)
                .fillMaxSize()
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier
                    .padding(bottom = MaterialTheme.spacing.large2)
                    .clip(CircleShape)
                    .background(iconBackgroundColor)
                    .size(46.dp)
                    .padding(12.dp),
                tint = Color.Black.copy(alpha = 0.8f)
            )
            Text(
                text = stringResource(id = title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = subtitle),
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = Dark
            )
        }
    }
}