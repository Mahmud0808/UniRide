package com.drdisagree.uniride.data.models

import android.os.Parcelable
import androidx.compose.runtime.Stable
import com.drdisagree.uniride.data.enums.BusStatus
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Stable
@Parcelize
data class RunningBus(
    val uuid: String = UUID.randomUUID().toString(),
    val bus: Bus,
    val category: BusCategory? = null,
    val driver: Driver? = null,
    val status: BusStatus? = null,
    val departedFrom: Place? = null,
    val departedTo: Place? = null,
    val departedAt: Long? = null,
    val reachedAt: Long? = null,
    val currentlyAt: LatLngSerializable? = null,
    val busFull: Boolean = false
) : Parcelable {
    constructor() : this(
        bus = Bus()
    )
}