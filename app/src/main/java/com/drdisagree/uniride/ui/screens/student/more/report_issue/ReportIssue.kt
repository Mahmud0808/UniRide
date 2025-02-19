package com.drdisagree.uniride.ui.screens.student.more.report_issue

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.drdisagree.uniride.R
import com.drdisagree.uniride.data.events.Resource
import com.drdisagree.uniride.data.models.Issue
import com.drdisagree.uniride.data.models.Student
import com.drdisagree.uniride.ui.components.navigation.MoreNavGraph
import com.drdisagree.uniride.ui.components.transitions.FadeInOutTransition
import com.drdisagree.uniride.ui.components.views.ButtonPrimary
import com.drdisagree.uniride.ui.components.views.Container
import com.drdisagree.uniride.ui.components.views.LoadingDialog
import com.drdisagree.uniride.ui.components.views.StyledRadioButton
import com.drdisagree.uniride.ui.components.views.StyledTextField
import com.drdisagree.uniride.ui.components.views.TopAppBarWithBackButton
import com.drdisagree.uniride.ui.screens.student.account.StudentSignInViewModel
import com.drdisagree.uniride.ui.theme.spacing
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@MoreNavGraph
@Destination(style = FadeInOutTransition::class)
@Composable
fun ReportIssue(
    navigator: DestinationsNavigator,
    student: Student
) {
    Container(shadow = false) {
        Scaffold(
            topBar = {
                TopAppBarWithBackButton(
                    title = stringResource(id = R.string.report_title),
                    onBackClick = {
                        navigator.navigateUp()
                    }
                )
            },
            content = { paddingValues ->
                ReportIssueContent(
                    paddingValues = paddingValues,
                    navigator = navigator,
                    student = student
                )
            }
        )
    }
}

@Composable
private fun ReportIssueContent(
    paddingValues: PaddingValues,
    navigator: DestinationsNavigator,
    student: Student
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.medium1)
    ) {
        ReportIssueFields(
            student = student
        )
    }
}

@Composable
private fun ReportIssueFields(
    student: Student,
    studentSignInViewModel: StudentSignInViewModel = hiltViewModel(),
    reportIssueViewModel: ReportIssueViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var issueDescription by rememberSaveable { mutableStateOf("") }
    val issueTypes = listOf(
        stringResource(R.string.bus_schedule),
        stringResource(R.string.route_details),
        stringResource(R.string.live_location_tracking),
        stringResource(R.string.driver_information),
        stringResource(R.string.other)
    )
    var selectedIssueTypeIndex by remember { mutableStateOf<Int?>(null) }
    fun onOptionSelected(index: Int) {
        selectedIssueTypeIndex = index
    }

    var contactInformation by rememberSaveable { mutableStateOf(student.email ?: "") }

    Text(
        text = stringResource(R.string.report_issue_ques_one),
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    )
    StyledRadioButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = MaterialTheme.spacing.small2
            ),
        options = issueTypes,
        selectedIndex = selectedIssueTypeIndex,
        onOptionSelected = ::onOptionSelected
    )

    Text(
        text = stringResource(R.string.report_issue_ques_two),
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        modifier = Modifier.padding(
            top = MaterialTheme.spacing.medium1
        )
    )
    StyledTextField(
        placeholder = stringResource(R.string.describe_the_issue_here),
        modifier = Modifier.padding(
            top = MaterialTheme.spacing.small2
        ),
        onValueChange = { issueDescription = it },
        inputText = issueDescription,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = false,
        minLines = 5
    )

    Text(
        text = stringResource(R.string.report_issue_ques_three),
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        modifier = Modifier.padding(
            top = MaterialTheme.spacing.medium1
        )
    )
    StyledTextField(
        placeholder = stringResource(R.string.email_or_phone_number),
        modifier = Modifier.padding(
            top = MaterialTheme.spacing.small2
        ),
        onValueChange = { contactInformation = it },
        inputText = contactInformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = true
    )

    ButtonPrimary(
        modifier = Modifier
            .padding(
                vertical = MaterialTheme.spacing.medium1,
            )
            .fillMaxWidth(),
        text = stringResource(R.string.submit)
    ) {
        if (
            selectedIssueTypeIndex == null ||
            issueDescription.trim().isEmpty() ||
            contactInformation.trim().isEmpty()
        ) {
            Toast.makeText(
                context,
                "Please fill in all fields",
                Toast.LENGTH_SHORT
            ).show()

            return@ButtonPrimary
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(contactInformation).matches()
            && !Patterns.PHONE.matcher(contactInformation).matches()
        ) {
            Toast.makeText(
                context,
                "Please enter a valid email or phone number",
                Toast.LENGTH_SHORT
            ).show()

            return@ButtonPrimary
        }

        reportIssueViewModel.submitIssue(
            Issue(
                type = issueTypes[selectedIssueTypeIndex!!],
                description = issueDescription.trim(),
                contactInfo = contactInformation.trim()
            )
        )
    }

    var showLoadingDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        reportIssueViewModel.state.collect { result ->
            when (result) {
                is Resource.Loading -> {
                    showLoadingDialog = true
                }

                is Resource.Success -> {
                    showLoadingDialog = false
                    selectedIssueTypeIndex = null
                    issueDescription = ""
                    contactInformation = ""

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