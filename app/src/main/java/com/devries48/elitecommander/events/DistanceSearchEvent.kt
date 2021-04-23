package com.devries48.elitecommander.events

data class DistanceSearchEvent(
    val success: Boolean, val distance: Double, val startSystemName: String,
    val endSystemName: String
)