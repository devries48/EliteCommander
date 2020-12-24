package com.devries48.elitecommander.frontier

data class FrontierTokensEvent(
    val success: Boolean, val accessToken: String,
    val refreshToken: String,
)