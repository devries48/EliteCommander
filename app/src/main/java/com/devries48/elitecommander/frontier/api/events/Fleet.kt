package com.devries48.elitecommander.frontier.api.events

data class Fleet(val success: Boolean, val ships: List<Ship>)
data class Ship(val id:Int, val model: String, val name: String?, val systemName: String,
                val stationName: String, val hullValue: Long, val modulesValue: Long,
                val cargoValue: Long, val totalValue: Long, val isCurrentShip: Boolean)