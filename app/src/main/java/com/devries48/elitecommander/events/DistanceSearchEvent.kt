package com.devries48.elitecommander.events

data class DistanceSearchEvent(val success: Boolean, val distance: Float, val startSystemName: String,
                               val endSystemName: String, val startPermitRequired: Boolean,
                               val endPermitRequired: Boolean)