package com.devries48.elitecommander.events

data class FrontierCreditsEvent(val success: Boolean, val balance: Long, val loan: Long)
