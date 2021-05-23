package com.devries48.elitecommander.events

data class FrontierProfileEvent(
    val success: Boolean,
    val error: String?,
    val name: String,
    val balance: Long,
    val loan: Long,
    val systemName: String,
    val hull: Int,
    val integrity: Int
)
