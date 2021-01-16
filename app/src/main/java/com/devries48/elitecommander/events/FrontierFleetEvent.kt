package com.devries48.elitecommander.events

data class FrontierFleetEvent(val success: Boolean, val frontierShips: List<FrontierShip>)

data class FrontierShip(val id:Int, val model: String, val name: String?, val systemName: String,
                        val stationName: String, val hullValue: Long, val modulesValue: Long,
                        val cargoValue: Long, val totalValue: Long, val isCurrentShip: Boolean)