package com.devries48.elitecommander.events

import androidx.annotation.StringRes

data class AlertEvent(val title: String?, @StringRes var message: Int)
