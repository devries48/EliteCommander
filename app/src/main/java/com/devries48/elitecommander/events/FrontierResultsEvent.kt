package com.devries48.elitecommander.events

data class FrontierResultsEvent<TDataType>(val success: Boolean, val results: List<TDataType>)
