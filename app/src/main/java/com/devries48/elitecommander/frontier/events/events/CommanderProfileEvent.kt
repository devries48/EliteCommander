package com.devries48.elitecommander.frontier.events.events

data class CommanderProfileEvent(
    val success: Boolean,
    val name: String,
    val balance: Long,
    val loan: Long,
    val systemName: String
)
