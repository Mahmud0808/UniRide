package com.drdisagree.uniride.ui.screens.driver.login.utils

sealed class LoginValidation {
    data object Valid : LoginValidation()
    data class Invalid(val error: String) : LoginValidation()
}