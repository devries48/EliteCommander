package com.devries48.elitecommander.frontier.events.events

data class FrontierTokensEvent(
    val success: Boolean, val accessToken: String,
    val refreshToken: String,
)