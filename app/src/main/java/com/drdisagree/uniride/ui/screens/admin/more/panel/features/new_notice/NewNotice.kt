package com.drdisagree.uniride.ui.screens.admin.more.panel.features.new_notice

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.drdisagree.uniride.R
import com.drdisagree.uniride.data.events.Resource
import com.drdisagree.uniride.data.models.Notice
import com.drdisagree.uniride.ui.components.navigation.MoreNavGraph
import com.drdisagree.uniride.ui.components.transitions.FadeInOutTransition
import com.drdisagree.uniride.ui.components.views.ButtonPrimary
import com.drdisagree.uniride.ui.components.views.Container
import com.drdisagree.uniride.ui.components.views.LoadingDialog
import com.drdisagree.uniride.ui.components.views.StyledTextField
import com.drdisagree.uniride.ui.components.views.TopAppBarWithBackButton
import com.drdisagree.uniride.ui.theme.spacing
import com.drdisagree.uniride.utils.viewmodels.AccountStatusViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@MoreNavGraph
@Destination(style = FadeInOutTransition::class)
@Composable
fun NewNotice(
    navigator: DestinationsNavigator
) {
    Container(shadow = false) {
        Scaffold(
            topBar = {
                TopAppBarWithBackButton(
                    title = stringResource(id = R.string.post_notice),
                    onBackClick = {
                        navigator.navigateUp()
                    }
                )
            },
            content = { paddingValues ->
                NewNoticeContent(
                    paddingValues = paddingValues,
                    navigator = navigator
                )
            }
        )
    }
}

@Composable
private fun NewNoticeContent(
    paddingValues: PaddingValues,
    navigator: DestinationsNavigator,
    accountStatusViewModel: AccountStatusViewModel = hiltViewModel()
) {
    val isAdminState by accountStatusViewModel.isAdmin.collectAsState()

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
    newNoticeViewModel: NewNoticeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var announcement by rememberSaveable { mutableStateOf("") }

    StyledTextField(
        placeholder = "Write your announcement here...",
        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small2),
        onValueChange = { announcement = it },
        inputText = announcement,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = false,
        minLines = 5
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
        text = "Post"
    ) {
        if (
            announcement.trim().isEmpty()
        ) {
            Toast.makeText(
                context,
                "Announcement cannot be empty",
                Toast.LENGTH_SHORT
            ).show()

            return@ButtonPrimary
        }

        newNoticeViewModel.postAnnouncement(
            Notice(
                announcement = announcement
            )
        )
    }

    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        newNoticeViewModel.state.collect { result ->
            when (result) {
                is Resource.Loading -> {
                    showLoadingDialog = true
                }

                is Resource.Success -> {
                    showLoadingDialog = false

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