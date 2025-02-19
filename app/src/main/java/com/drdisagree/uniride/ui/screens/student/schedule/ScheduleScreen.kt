package com.drdisagree.uniride.ui.screens.student.schedule

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.drdisagree.uniride.R
import com.drdisagree.uniride.data.events.Resource
import com.drdisagree.uniride.data.models.Schedule
import com.drdisagree.uniride.ui.components.navigation.ScheduleNavGraph
import com.drdisagree.uniride.ui.components.transitions.FadeInOutTransition
import com.drdisagree.uniride.ui.components.views.Container
import com.drdisagree.uniride.ui.components.views.TopAppBarWithBackButtonAndEndIcon
import com.drdisagree.uniride.ui.screens.destinations.EditScheduleDestination
import com.drdisagree.uniride.ui.screens.destinations.ScheduleSearchScreenDestination
import com.drdisagree.uniride.ui.theme.Dark
import com.drdisagree.uniride.ui.theme.LightGray
import com.drdisagree.uniride.ui.theme.spacing
import com.drdisagree.uniride.utils.ColorUtils.getBusIconColor
import com.drdisagree.uniride.utils.ColorUtils.getSchedulePillColors
import com.drdisagree.uniride.viewmodels.AccountStatusViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

@ScheduleNavGraph(start = true)
@Destination(style = FadeInOutTransition::class)
@Composable
fun ScheduleScreen(
    navigator: DestinationsNavigator
) {
    Container(shadow = false) {
        Scaffold(
            topBar = {
                TopAppBarWithBackButtonAndEndIcon(
                    title = stringResource(id = R.string.nav_schedule),
                    onBackClick = {
                        navigator.navigateUp()
                    },
                    endIcon = {
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = stringResource(R.string.search),
                                tint = Color.Black.copy(alpha = 0.8f)
                            )
                        }
                    },
                    endIconClick = {
                        navigator.navigate(
                            ScheduleSearchScreenDestination()
                        )
                    }
                )
            },
            content = { paddingValues ->
                ScheduleContent(
                    navigator = navigator,
                    paddingValues = paddingValues
                )
            }
        )
    }
}

@Composable
private fun ScheduleContent(
    navigator: DestinationsNavigator,
    paddingValues: PaddingValues,
    scheduleViewModel: ScheduleViewModel = hiltViewModel(),
    accountStatusViewModel: AccountStatusViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isAdminState by accountStatusViewModel.isAdmin.collectAsState()
    val schedules by scheduleViewModel.allSchedules.collectAsState()

    when (schedules) {
        is Resource.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
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

        is Resource.Success -> {
            val scheduleList = schedules.data

            if (!scheduleList.isNullOrEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = paddingValues)
                ) {
                    items(
                        count = scheduleList.size,
                        key = { scheduleList[it].uuid }
                    ) { index ->
                        ScheduleListItem(
                            index = index,
                            navigator = navigator,
                            schedule = scheduleList[index],
                            isAdmin = isAdminState ?: false
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
                        text = stringResource(R.string.schedule_is_empty)
                    )
                }
            }
        }

        is Resource.Error -> {
            LaunchedEffect(schedules.message) {
                schedules.message?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            }
        }

        else -> {}
    }
}

@Composable
fun ScheduleListItem(
    modifier: Modifier = Modifier,
    navigator: DestinationsNavigator,
    index: Int,
    schedule: Schedule,
    isAdmin: Boolean
) {
    val busIconColor = remember(schedule.bus.name) {
        getBusIconColor(schedule.bus.name)
    }
    val (categoryPillBackgroundColor, categoryPillTextColor) = remember(schedule.category.name) {
        getSchedulePillColors(schedule.category.name)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (isAdmin) {
                    Modifier.clickable {
                        navigator.navigate(
                            EditScheduleDestination(schedule)
                        )
                    }
                } else {
                    Modifier
                }
            )
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
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_bus),
                contentDescription = "Bus icon",
                colorFilter = ColorFilter.tint(color = busIconColor),
                modifier = Modifier
                    .padding(end = 16.dp, top = 2.dp)
                    .size(28.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = MaterialTheme.spacing.medium1),
                verticalArrangement = Arrangement.Top,
            ) {
                Text(
                    text = schedule.bus.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        ) {
                            append(stringResource(R.string.from_colon))
                        }
                        append(schedule.from.name)
                    },
                    color = Dark,
                    fontSize = 14.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        ) {
                            append(stringResource(R.string.to_colon))
                        }
                        append(schedule.to.name)
                    },
                    color = Dark,
                    fontSize = 14.sp
                )
            }

            Column(
                modifier = Modifier.width(IntrinsicSize.Min),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.spacing.small1)
                        .clip(RoundedCornerShape(28.dp))
                        .background(categoryPillBackgroundColor)
                        .padding(
                            horizontal = MaterialTheme.spacing.small2,
                            vertical = MaterialTheme.spacing.extraSmall1
                        )
                ) {
                    Text(
                        text = schedule.category.name,
                        color = categoryPillTextColor,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = schedule.time,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = MaterialTheme.spacing.small2)
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun sortSchedulesByTime(schedules: List<Schedule>, is24HourFormat: Boolean): List<Schedule> {
    val formatter = DateTimeFormatter.ofPattern(
        if (is24HourFormat) "HH:mm" else "hh:mm a",
        Locale.getDefault()
    )

    return schedules.sortedWith(
        compareBy<Schedule> { schedule ->
            LocalTime.parse(schedule.time, formatter)
        }.thenBy { schedule ->
            naturalOrderCompare(schedule.bus.name)
        }
    )
}

@SuppressLint("DefaultLocale")
fun naturalOrderCompare(name: String): String {
    val pattern = Pattern.compile("(\\d+)")
    val matcher = pattern.matcher(name)

    val result = StringBuilder()
    var lastIndex = 0

    while (matcher.find()) {
        val number = matcher.group()
        val nonNumericPart = name.substring(lastIndex, matcher.start())

        result.append(nonNumericPart)
        result.append(String.format("%05d", number.toInt()))

        lastIndex = matcher.end()
    }

    result.append(name.substring(lastIndex))

    return result.toString()
}