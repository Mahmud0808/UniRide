package com.drdisagree.uniride.data.models

import android.os.Parcelable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parcelize

@Stable
@Parcelize
data class Route(
    val routeNo: String,
    val routeName: String,
    val routeDetails: String,
    val startTime: String,
    val departureTime: String,
    val routeWebUrl: String
) : Parcelable {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        ""
    )
}