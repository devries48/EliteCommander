package com.devries48.elitecommander.events

data class DistanceSearch(val success: Boolean, val distance: Float, val startSystemName: String,
                          val endSystemName: String, val startPermitRequired: Boolean,
                          val endPermitRequired: Boolean)